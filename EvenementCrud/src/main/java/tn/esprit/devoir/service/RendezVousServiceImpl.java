package tn.esprit.devoir.service;

import org.springframework.stereotype.Service;
import tn.esprit.devoir.dto.CandidatureSummaryDTO;
import tn.esprit.devoir.dto.RendezVousDTO;
import tn.esprit.devoir.entite.Candidature;
import tn.esprit.devoir.entite.RendezVous;
import tn.esprit.devoir.entite.StatutCandidature;
import tn.esprit.devoir.entite.StatutRendezVous;
import tn.esprit.devoir.repository.CandidatureRepository;
import tn.esprit.devoir.repository.RendezVousRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final CandidatureRepository candidatureRepository;
    private final WebhookService webhookService;

    public RendezVousServiceImpl(RendezVousRepository rendezVousRepository,
                                 CandidatureRepository candidatureRepository,
                                 WebhookService webhookService) {
        this.rendezVousRepository = rendezVousRepository;
        this.candidatureRepository = candidatureRepository;
        this.webhookService = webhookService;
    }

    // Génère un lien visio unique (exemple simple avec UUID)
    private String genererLienVisio() {
        return "https://meet.google.com/" + UUID.randomUUID().toString().substring(0, 10);
    }

    @Override
    public RendezVous getRendezVousByToken(String token) {
        return rendezVousRepository.findByTokenAcces(token)
            .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable ou lien expiré"));
    }

    @Override
    public RendezVous creerRendezVous(RendezVous rdv) {
        rdv.setTokenAcces(UUID.randomUUID().toString());

        // Générer lien visio si absent
        if (rdv.getLienVisio() == null || rdv.getLienVisio().isEmpty()) {
            rdv.setLienVisio(genererLienVisio());
        }

        return rendezVousRepository.save(rdv);
    }

    @Override
    public RendezVous associerCandidature(Long rdvId, Long candidatureId) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
            .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));
        Candidature cand = candidatureRepository.findById(candidatureId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        rdv.setCandidature(cand);
        return rendezVousRepository.save(rdv);
    }

    @Override
    public RendezVousDTO getRendezVousDTOByToken(String token) {
        RendezVous rdv = getRendezVousByToken(token);
        return new RendezVousDTO(rdv);
    }

    @Override
    public List<RendezVousDTO> getRendezVousPlanifiesAvecCandidature() {
        return rendezVousRepository.findAll().stream()
            .filter(rdv ->
                rdv.getCandidature() != null &&
                    (rdv.getStatut() == StatutRendezVous.PLANIFIE ||
                        rdv.getStatut() == StatutRendezVous.ANNULE ||
                        rdv.getStatut() == StatutRendezVous.TERMINE) &&
                    rdv.getCandidature().getStatut() == StatutCandidature.ENTRETIEN_PLANIFIE
            )
            .map(rdv -> {
                RendezVousDTO dto = new RendezVousDTO(rdv);
                Candidature c = rdv.getCandidature();
                if (c != null && c.getCandidat() != null) {
                    String nomOffre = null;
                    if (c.getOffre() != null) {
                        nomOffre = c.getOffre().getTitre();
                    }
                    dto.setCandidature(new CandidatureSummaryDTO(
                        c.getId(),
                        c.getCandidat().getFirstName(),
                        c.getCandidat().getLastName(),
                        c.getCandidat().getEmail(),
                        c.getImageUrl(),
                        nomOffre
                    ));
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public RendezVous modifierStatut(Long id, String nouveauStatut) {
        RendezVous rdv = rendezVousRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec l'id : " + id));

        rdv.setStatut(StatutRendezVous.valueOf(nouveauStatut));
        return rendezVousRepository.save(rdv);
    }

    @Override
    public RendezVous updateRendezVousFromDTO(Long id, RendezVousDTO dto) {
        RendezVous rdv = rendezVousRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable avec l'id : " + id));

        if (dto.getDateHeure() != null) {
            rdv.setDateHeure(dto.getDateHeure());
        }

        if (dto.getStatut() != null) {
            rdv.setStatut(StatutRendezVous.valueOf(dto.getStatut()));
        }

        if (dto.getLienVisio() != null) {
            rdv.setLienVisio(dto.getLienVisio());
        }

        if (dto.getType() != null) {
            rdv.setType(dto.getType());
        }

        if (dto.getActif() != null) {
            rdv.setActif(dto.getActif());
        }

        if (dto.getCandidatureId() != null) {
            Candidature cand = candidatureRepository.findById(dto.getCandidatureId())
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + dto.getCandidatureId()));
            rdv.setCandidature(cand);
        }

        // Générer lien visio automatiquement si statut PLANIFIE et lien absent
        if (rdv.getStatut() == StatutRendezVous.PLANIFIE && (rdv.getLienVisio() == null || rdv.getLienVisio().isEmpty())) {
            rdv.setLienVisio(genererLienVisio());
        }

        RendezVous savedRdv = rendezVousRepository.save(rdv);

        // Appel webhook n8n si statut PLANIFIE
        if (savedRdv.getStatut() == StatutRendezVous.PLANIFIE && savedRdv.getCandidature() != null) {
            String email = savedRdv.getCandidature().getCandidat().getEmail();
            String nom = savedRdv.getCandidature().getCandidat().getFirstName();
            String dateHeure = savedRdv.getDateHeure().toString();
            String lienMeet = savedRdv.getLienVisio();

            webhookService.sendEntretienPlanifieToN8n(nom, email, dateHeure, lienMeet);
        }

        return savedRdv;
    }

    @Override
    public void supprimerRendezVous(Long id) {
        RendezVous rdv = rendezVousRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé avec l'ID : " + id));
        rendezVousRepository.delete(rdv);
    }
}
