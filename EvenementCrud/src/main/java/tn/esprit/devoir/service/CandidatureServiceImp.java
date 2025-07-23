package tn.esprit.devoir.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.dto.CandidatureDto;
import tn.esprit.devoir.dto.ScoreResponse;
import tn.esprit.devoir.entite.Candidature;
import tn.esprit.devoir.entite.RendezVous;
import tn.esprit.devoir.entite.StatutCandidature;
import tn.esprit.devoir.repository.CandidatureRepository;
import tn.esprit.devoir.repository.OffreRepository;
import tn.esprit.devoir.repository.RendezVousRepository;
import tn.esprit.devoir.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidatureServiceImp implements CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final RendezVousRepository rendezVousRepository;
    private final EmailService emailService;
    private final WebhookService webhookService;  // <-- Ajouté ici
    @Autowired
    private OffreRepository offreRepository;
    @Autowired
    private CamundaService camundaService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String flaskUrl = "http://localhost:5051/analyze";


    // Un seul constructeur avec toutes les dépendances injectées
    public CandidatureServiceImp(CandidatureRepository candidatureRepository,
                                 UserRepository userRepository,
                                 Cloudinary cloudinary,
                                 RendezVousRepository rendezVousRepository,
                                 EmailService emailService,
                                 WebhookService webhookService) {  // <-- injection ici
        this.candidatureRepository = candidatureRepository;
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
        this.rendezVousRepository = rendezVousRepository;
        this.emailService = emailService;
        this.webhookService = webhookService;
    }

    @Override
    public List<Candidature> getAllCandidaturesWithCandidat() {
        return candidatureRepository.findAllWithCandidat();
    }

    @Override
    public Candidature modifierCandidature(Long id, CandidatureDto dto) {
        Candidature c = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        if (dto.getEmail() != null) {
            c.setEmail(dto.getEmail());
        }
        if (dto.getTelephone() != null) {
            c.setTelephone(dto.getTelephone());
        }

        if (dto.getStatut() != null && dto.getStatut() != c.getStatut()) {
            c.setStatut(dto.getStatut());

            if (dto.getStatut() == StatutCandidature.ENTRETIEN_PLANIFIE) {
                boolean rdvExiste = rendezVousRepository.existsByCandidature(c);
                if (!rdvExiste) {
                    RendezVous rdv = new RendezVous();
                    rdv.setCandidature(c);
                    rdv.setDateHeure(c.getDateEntretien() != null
                        ? c.getDateEntretien()
                        : LocalDateTime.now().plusDays(1));
                    rdv.setTokenAcces(UUID.randomUUID().toString());
                    rdv.setStatut(tn.esprit.devoir.entite.StatutRendezVous.PLANIFIE);
                    rdv.setActif(true);
                    rendezVousRepository.save(rdv);

                    String lienEntretien = "http://localhost:4200/entretien/" + rdv.getTokenAcces();
                    emailService.envoyerInvitationEntretien(
                        c.getEmail(),
                        rdv.getDateHeure(),
                        lienEntretien
                    );
                }
            }
        }

        return candidatureRepository.save(c);
    }

    @Override
    public Candidature postuler(Candidature candidature) {
        Long candidatId = candidature.getCandidatId();
        Long offreId = candidature.getOffreId();

        // Vérification : empêche le candidat de postuler plusieurs fois à la même offre
        if (candidatureRepository.existsByCandidatIdAndOffreId(candidatId, offreId)) {
            throw new RuntimeException("Vous avez déjà postulé à cette offre.");
        }

        // Associer le candidat et l’offre
        userRepository.findById(Math.toIntExact(candidatId)).ifPresent(candidature::setCandidat);
        offreRepository.findById(offreId).ifPresent(candidature::setOffre);

        candidature.setDateCandidature(LocalDateTime.now());
        candidature.setStatut(StatutCandidature.EN_ATTENTE);

        Candidature saved = candidatureRepository.save(candidature);

        // Infos pour webhook
        String nom = "";
        String prenom = "";
        String email = "";

        if (saved.getCandidat() != null) {
            nom = saved.getCandidat().getFirstName() + " " + saved.getCandidat().getLastName();
            prenom = saved.getCandidat().getFirstName();
            email = saved.getCandidat().getEmail();
        }

        String titreOffre = (saved.getOffre() != null)
            ? saved.getOffre().getTitre()
            : "Offre inconnue";

        webhookService.sendCandidatureToN8n(nom, prenom, email, titreOffre);

        // Lancer le process Camunda une seule fois
        camundaService.demarrerValidationCandidature(saved.getId(), titreOffre);

        return saved;
    }


    @Override
    public Candidature postulerEtAnalyser(Candidature candidature) {
        // 1. Initialisation candidature
        candidature.setDateCandidature(LocalDateTime.now());
        candidature.setStatut(StatutCandidature.EN_ATTENTE);

        // 2. Sauvegarde initiale
        Candidature saved = candidatureRepository.save(candidature);

        // 3. Préparer la requête vers Flask
        Map<String, Object> request = new HashMap<>();
        request.put("candidatureId", saved.getId());
        request.put("offreId", saved.getOffreId());
        request.put("cvFilePath", saved.getCvUrl()); // Adapter selon ta gestion réelle

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.valueOf(MediaType.APPLICATION_JSON));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // 4. Appel au service Flask
            ResponseEntity<ScoreResponse> response = restTemplate.postForEntity(flaskUrl, entity, ScoreResponse.class);
            ScoreResponse scoreResponse = response.getBody();

            if (scoreResponse != null) {
                // 5. Mise à jour score et matched
                saved.setScore(scoreResponse.getScore());
                saved.setMatched(scoreResponse.getMatched());

                // 6. Démarrer process Camunda avec variable "score"
                Map<String, Object> variables = new HashMap<>();
                variables.put("score", scoreResponse.getScore());

                String processInstanceId = camundaService.demarrerValidationCandidatureAvecVariables(
                    saved.getId(),
                    saved.getOffre().getTitre(),
                    variables
                );

                // 7. Stocker id process Camunda
                saved.setProcessInstanceId(processInstanceId);

                // 8. Sauvegarder candidature mise à jour
                candidatureRepository.save(saved);
            }

            return saved;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'analyse du CV", e);
        }
    }
    @Override
    public List<Long> getOffresIdPostuleesParCandidat(Long candidatId) {
        return candidatureRepository.findByCandidatId(candidatId)
            .stream()
            .map(c -> c.getOffre().getId())
            .collect(Collectors.toList());
    }




    @Override
    public List<Candidature> getCandidaturesByUserId(Long userId) {
        return candidatureRepository.findByCandidatId(userId);
    }

    @Override
    public Optional<Candidature> getCandidatureById(Long id) {
        return candidatureRepository.findById(id);
    }

    @Override
    public Candidature mettreAJourStatut(Long id, StatutCandidature statut, LocalDateTime dateEntretien) {
        Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        candidature.setStatut(statut);
        candidature.setDateMiseAJour(LocalDateTime.now());

        if (statut == StatutCandidature.ENTRETIEN_PLANIFIE) {
            candidature.setDateEntretien(dateEntretien);

            boolean rdvExiste = rendezVousRepository.existsByCandidature(candidature);
            if (!rdvExiste) {
                RendezVous rdv = new RendezVous();
                rdv.setCandidature(candidature);
                rdv.setDateHeure(dateEntretien != null ? dateEntretien : LocalDateTime.now().plusDays(1));
                rdv.setTokenAcces(UUID.randomUUID().toString());
                rdv.setStatut(tn.esprit.devoir.entite.StatutRendezVous.PLANIFIE);
                rdv.setActif(true);
                rendezVousRepository.save(rdv);

                String lienEntretien = "http://localhost:4200/entretien/" + rdv.getTokenAcces();

                emailService.envoyerInvitationEntretien(
                    candidature.getEmail(),
                    rdv.getDateHeure(),
                    lienEntretien
                );
            }
        }

        return candidatureRepository.save(candidature);
    }

    @Override
    public List<Candidature> getCandidaturesParOffre(Long offreId) {
        return candidatureRepository.findByOffreId(offreId);
    }







    @Override
    public void supprimerCandidature(Long id) {
        candidatureRepository.deleteById(id);
    }

    @Override
    public List<CandidatureDto> getCandidaturesParCandidat(Long candidatId) {
        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidatId);
        return candidatures.stream().map(c -> {
            CandidatureDto dto = new CandidatureDto(c);
            dto.setImageProfilUrl(c.getImageUrl());

            Integer userId = candidatId != null ? candidatId.intValue() : null;
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    dto.setEmail(user.getEmail());
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String uploadDir = new File("upload-directory/cv").getAbsolutePath();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
                : "file";

            String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
            File destinationFile = new File(dir, uniqueFilename);

            file.transferTo(destinationFile);

            // retourne un chemin relatif depuis upload-directory/cv, tu peux adapter selon besoin
            return "cv/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier", e);
        }
    }


    @Override
    public void planifierEntretien(Long idCandidature, LocalDateTime dateEntretien) {
        Candidature candidature = candidatureRepository.findById(idCandidature)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        candidature.setStatut(StatutCandidature.ENTRETIEN_PLANIFIE);
        candidature.setDateMiseAJour(LocalDateTime.now());
        candidatureRepository.save(candidature);

        RendezVous rdv = new RendezVous();
        rdv.setCandidature(candidature);
        rdv.setDateHeure(dateEntretien);
        rdv.setTokenAcces(UUID.randomUUID().toString());
        rendezVousRepository.save(rdv);

        String lienEntretien = "http://localhost:4200/entretien/" + rdv.getTokenAcces();

        emailService.envoyerInvitationEntretien(
            candidature.getEmail(),
            dateEntretien,
            lienEntretien
        );
    }

    @Override
    public boolean demarrerValidationCandidature(Long id, String titre) {
        // Délègue à CamundaService la logique pour démarrer le process Camunda
        return camundaService.demarrerValidationCandidature(id, titre);
    }

    @Override
    public String getTitreOffreParId(Long offreId) {
        return offreRepository.findById(offreId)
            .map(offre -> offre.getTitre())
            .orElse("Offre inconnue");
    }

    @Override
    public List<CandidatureDto> getHistoriqueCandidatures() {
        List<Candidature> candidatures = candidatureRepository.findAllByOrderByDateCandidatureDesc();
        return candidatures.stream()
            .map(c -> {
                CandidatureDto dto = new CandidatureDto(c);
                dto.setImageProfilUrl(c.getImageUrl());
                // Optionnel : récupérer l’email ou autre info utilisateur
                if (c.getCandidat() != null) {
                    dto.setEmail(c.getCandidat().getEmail());
                }
                return dto;
            })
            .collect(Collectors.toList());
    }


}
