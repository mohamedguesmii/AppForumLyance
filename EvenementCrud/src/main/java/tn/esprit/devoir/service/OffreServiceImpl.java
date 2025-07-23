package tn.esprit.devoir.service;

import tn.esprit.devoir.dto.OffreDto;
import tn.esprit.devoir.dto.OffreUpdateDto;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.entite.Offre;
import tn.esprit.devoir.entite.StatutOffre;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.repository.EvenementRepo;
import tn.esprit.devoir.repository.OffreRepository;
import tn.esprit.devoir.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OffreServiceImpl extends OffreService {

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EvenementRepo evenementRepo;

    @Autowired
    private CamundaService camundaService;

    @Override
    public Offre ajouterOffre(Offre offre, Integer createurId, Long evenementId) {
        User user = userRepository.findById(createurId)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé avec l'id: " + createurId));
        offre.setCreateur(user);
        offre.setDatePublication(LocalDate.now());

        if (evenementId != null) {
            Evenement evenement = evenementRepo.findById(evenementId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable avec ID: " + evenementId));
            offre.setEvenement(evenement);
        }

        return offreRepository.save(offre);
    }

    public Offre ajouterOffreEtDemarrerProcess(Offre offre, Integer createurId, Long evenementId) {
        Offre savedOffre = ajouterOffre(offre, createurId, evenementId);
        boolean started = camundaService.demarrerValidationOffre(savedOffre.getId(), savedOffre.getTitre());
        if (!started) {
            System.err.println("⚠️ Échec démarrage process Camunda pour offre id " + savedOffre.getId());
        }
        return savedOffre;
    }

    @Override
    public List<Offre> getAllOffres() {
        return offreRepository.findAll();
    }

    @Override
    public Offre getOffreById(Long id) {
        return offreRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Offre avec id " + id + " non trouvée"));
    }

    @Override
    public void supprimerOffre(Long id) {
        offreRepository.deleteById(id);
    }

    @Override
    public Offre modifierOffre(Long id, OffreDto dto, Integer createurId) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Offre introuvable avec id: " + id));

        User user = userRepository.findById(createurId)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé avec l'id: " + createurId));
        offre.setCreateur(user);

        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setDomaine(dto.getDomaine());
        offre.setType(dto.getType());
        offre.setDuree(dto.getDuree());

        if (dto.getEvenementId() != null) {
            Evenement evenement = evenementRepo.findById(dto.getEvenementId())
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable avec id: " + dto.getEvenementId()));
            offre.setEvenement(evenement);
        } else {
            offre.setEvenement(null);
        }

        return offreRepository.save(offre);
    }

    public Offre modifierOffreAvecStatut(Long id, OffreUpdateDto dto, Integer createurId) {
        Offre offre = offreRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Offre introuvable avec id: " + id));

        User user = userRepository.findById(createurId)
            .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé avec l'id: " + createurId));
        offre.setCreateur(user);

        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());

        if (dto.getDomaine() != null) {
            offre.setDomaine(dto.getDomaine());
        }

        if (dto.getType() != null) {
            offre.setType(dto.getType());
        }

        if (dto.getDuree() != null) {
            offre.setDuree(dto.getDuree());
        }

        if (dto.getStatut() != null) {
            offre.setStatut(StatutOffre.valueOf(dto.getStatut().toUpperCase()));
        }

        if (dto.getEvenementId() != null) {
            Evenement evenement = evenementRepo.findById(dto.getEvenementId())
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable avec id: " + dto.getEvenementId()));
            offre.setEvenement(evenement);
        } else {
            offre.setEvenement(null);
        }

        return offreRepository.save(offre);
    }

    public List<OffreDto> getHistoriqueOffres() {
        List<StatutOffre> statutsHistorique = List.of(StatutOffre.VALIDÉ, StatutOffre.REFUSÉ);
        List<Offre> offres = offreRepository.findByStatutIn(statutsHistorique);
        return offres.stream().map(this::convertToDto).toList();
    }

    private OffreDto convertToDto(Offre offre) {
        OffreDto dto = new OffreDto();
        dto.setTitre(offre.getTitre());
        dto.setDescription(offre.getDescription());
        dto.setDomaine(offre.getDomaine());
        dto.setDatePublication(offre.getDatePublication().toString());
        dto.setType(offre.getType());
        dto.setDuree(offre.getDuree());
        dto.setStatut(offre.getStatut() != null ? offre.getStatut().name() : null);
        dto.setCreateurId(offre.getCreateurId() != null ? Long.valueOf(offre.getCreateurId()) : null);
        dto.setEvenementId(offre.getEvenementId());
        return dto;
    }
}
