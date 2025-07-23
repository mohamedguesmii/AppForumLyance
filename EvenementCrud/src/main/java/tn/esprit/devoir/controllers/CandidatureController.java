package tn.esprit.devoir.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.dto.CandidatureDto;
import tn.esprit.devoir.entite.Candidature;
import tn.esprit.devoir.entite.StatutCandidature;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.service.CamundaService;
import tn.esprit.devoir.service.CandidatureService;
import tn.esprit.devoir.service.UserService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    private static final Logger logger = LoggerFactory.getLogger(CandidatureController.class);

    private final CandidatureService candidatureService;
    private final UserService userService;

    @Autowired
    private CamundaService camundaService;

    @Autowired
    public CandidatureController(CandidatureService candidatureService, UserService userService) {
        this.candidatureService = candidatureService;
        this.userService = userService;
    }

    // POST - créer une candidature avec fichiers
    @PostMapping(path = "/postuler", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postuler(
        @RequestParam("candidatId") Long candidatId,
        @RequestParam("telephone") String telephone,
        @RequestParam(value = "commentaire", required = false) String commentaire,
        @RequestParam("cv") MultipartFile cv,
        @RequestParam("lettreMotivation") MultipartFile lettreMotivation,
        @RequestParam(value = "imageProfil", required = false) MultipartFile imageProfil,
        @RequestParam("offreId") Long offreId
    ) {
        logger.debug("Début postuler - candidatId={}", candidatId);
        try {
            Optional<User> candidatOpt = Optional.ofNullable(userService.getUserById(candidatId.intValue()));
            if (candidatOpt.isEmpty()) {
                logger.warn("Candidat inexistant, id={}", candidatId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Candidat avec id " + candidatId + " introuvable");
            }

            Candidature candidature = new Candidature();
            candidature.setCandidatId(candidatId);
            candidature.setTelephone(telephone);
            candidature.setCommentairesRecruteur(commentaire);
            candidature.setDateCandidature(LocalDateTime.now());
            candidature.setStatut(StatutCandidature.EN_ATTENTE);
            candidature.setOffreId(offreId);

            // Seul le CV est validé et stocké
            if (cv == null || cv.isEmpty() || cv.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CV est obligatoire et < 5Mo");
            }
            String cvUrl = candidatureService.uploadFile(cv);
            candidature.setCvUrl(cvUrl);
            candidature.setCvData(null);

            // Lettre de motivation et image sont seulement validées mais ignorées
            if (lettreMotivation == null || lettreMotivation.isEmpty() || lettreMotivation.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lettre de motivation obligatoire et < 5Mo");
            }
            if (imageProfil != null && !imageProfil.isEmpty() && imageProfil.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image profil trop volumineuse (>5Mo)");
            }

            // Ne pas enregistrer lettreMotivation ni imageProfil dans la base
            candidature.setLettreMotivation(null);
            candidature.setLettreData(null);
            candidature.setImageUrl(null);
            candidature.setImageData(null);

            Candidature saved = candidatureService.postuler(candidature);
            logger.debug("Candidature sauvegardée avec ID={}", saved.getId());

            boolean started = candidatureService.demarrerValidationCandidature(saved.getId(), "Candidature " + saved.getId());
            if (!started) {
                logger.error("Erreur démarrage process Camunda pour candidature {}", saved.getId());
            } else {
                logger.debug("Process Camunda démarré pour candidature {}", saved.getId());
            }

            // Envoi du webhook (avec seulement les infos pertinentes)
            try {
                User candidat = candidatOpt.get();
                Map<String, Object> webhookPayload = new HashMap<>();
                webhookPayload.put("candidatId", saved.getCandidatId());
                webhookPayload.put("telephone", saved.getTelephone());
                webhookPayload.put("commentaire", saved.getCommentairesRecruteur());
                webhookPayload.put("cvUrl", saved.getCvUrl());
                webhookPayload.put("offreId", saved.getOffreId());
                webhookPayload.put("dateCandidature", saved.getDateCandidature().toString());
                webhookPayload.put("statut", saved.getStatut().name());
                webhookPayload.put("prenom", candidat.getFirstName());
                webhookPayload.put("nom", candidat.getLastName());
                webhookPayload.put("email", candidat.getEmail());

                RestTemplate restTemplate = new RestTemplate();
                String webhookUrl = "http://localhost:5678/webhook/candidature";
                restTemplate.postForEntity(webhookUrl, webhookPayload, String.class);

                logger.debug("Webhook envoyé avec succès pour candidature ID {}", saved.getId());
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi du webhook n8n :", e);
            }

            return new ResponseEntity<>(new CandidatureDto(saved), HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Exception dans postuler", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur serveur lors de la création de la candidature");
        }
    }


    @GetMapping("/cv-only/{candidatureId}")
    public ResponseEntity<?> getCvByCandidatureId(@PathVariable Long candidatureId) {
        Optional<Candidature> candidatureOpt = candidatureService.getCandidatureById(candidatureId);
        if (candidatureOpt.isPresent()) {
            String cvUrl = candidatureOpt.get().getCvUrl();
            if (cvUrl != null && !cvUrl.isEmpty()) {
                return ResponseEntity.ok().body(cvUrl); // retourne juste l'URL du CV
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun CV trouvé pour cette candidature");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Candidature introuvable");
        }
    }


    @GetMapping("/cv/{filename:.+}")
    public ResponseEntity<Resource> getCvFile(@PathVariable String filename) throws IOException {
        Path file = Paths.get("upload-directory/cv").resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
    }


    @GetMapping("/all")
    public ResponseEntity<List<Candidature>> getAllCandidatures() {
        return ResponseEntity.ok(candidatureService.getAllCandidaturesWithCandidat());
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<CandidatureDto>> getByCandidatId(@PathVariable Long candidatId) {
        List<CandidatureDto> candidaturesDto = candidatureService.getCandidaturesParCandidat(candidatId);
        return ResponseEntity.ok(candidaturesDto);
    }

    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Candidature>> getByOffre(@PathVariable Long offreId) {
        List<Candidature> list = candidatureService.getCandidaturesParOffre(offreId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidature> getById(@PathVariable Long id) {
        Optional<Candidature> candidatureOpt = candidatureService.getCandidatureById(id);
        return candidatureOpt.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<?> changerStatutEtDateEntretien(
        @PathVariable Long id,
        @RequestParam StatutCandidature statut,
        @RequestParam(required = false) String dateEntretien
    ) {
        try {
            LocalDateTime dateEntretienParsed = null;
            if (dateEntretien != null && !dateEntretien.isEmpty()) {
                dateEntretienParsed = LocalDateTime.parse(dateEntretien);
            }
            Candidature updated = candidatureService.mettreAJourStatut(id, statut, dateEntretienParsed);
            return ResponseEntity.ok(new CandidatureDto(updated));
        } catch (RuntimeException e) {
            logger.warn("Candidature non trouvée id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur dans changerStatutEtDateEntretien", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Format de date incorrect");
        }
    }

    @GetMapping("/offres-postulees/{candidatId}")
    public ResponseEntity<List<Long>> getOffresPostuleesParCandidat(@PathVariable Long candidatId) {
        List<Long> ids = candidatureService.getOffresIdPostuleesParCandidat(candidatId);
        return ResponseEntity.ok(ids);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage().contains("déjà postulé")) {
            return ResponseEntity.badRequest().body(ex.getMessage()); // HTTP 400
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Erreur interne du serveur");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierCandidature(
        @PathVariable Long id,
        @RequestBody CandidatureDto candidatureDto) {
        try {
            Candidature updated = candidatureService.modifierCandidature(id, candidatureDto);
            return ResponseEntity.ok(new CandidatureDto(updated));
        } catch (RuntimeException e) {
            logger.warn("Candidature non trouvée id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        Optional<Candidature> candidatureOpt = candidatureService.getCandidatureById(id);
        if (candidatureOpt.isPresent()) {
            candidatureService.supprimerCandidature(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tasks/rh")
    public ResponseEntity<List<Map<String, Object>>> getTasksForRh() {
        List<Map<String, Object>> tasks = camundaService.getTasksForRh();
        return ResponseEntity.ok(tasks);
    }

    // Historique des candidatures
    @GetMapping("/historique")
    public ResponseEntity<List<CandidatureDto>> getHistorique() {
        List<CandidatureDto> historique = candidatureService.getHistoriqueCandidatures();
        return ResponseEntity.ok(historique);
    }
}
