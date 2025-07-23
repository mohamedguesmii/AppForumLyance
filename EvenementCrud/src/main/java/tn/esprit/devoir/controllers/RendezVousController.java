package tn.esprit.devoir.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.CandidatureSummaryDTO;
import tn.esprit.devoir.dto.EmailEntretienRequest;
import tn.esprit.devoir.dto.RendezVousDTO;
import tn.esprit.devoir.entite.Candidature;
import tn.esprit.devoir.entite.RendezVous;
import tn.esprit.devoir.service.EmailService;
import tn.esprit.devoir.service.RendezVousService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/rendezvous")
@CrossOrigin(origins = "*")
public class RendezVousController {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousController.class);

    private final RendezVousService rendezVousService;
    private final EmailService emailService;

    public RendezVousController(RendezVousService rendezVousService, EmailService emailService) {
        this.rendezVousService = rendezVousService;
        this.emailService = emailService;
    }

    // Créer un rendez-vous
    @PostMapping
    public ResponseEntity<?> creerRendezVous(@RequestBody RendezVous rdv) {
        try {
            RendezVous nouveauRdv = rendezVousService.creerRendezVous(rdv);
            return ResponseEntity.ok(nouveauRdv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Récupérer un rendez-vous par token
    @GetMapping("/{token}")
    public ResponseEntity<?> getRendezVousByToken(@PathVariable String token) {
        try {
            RendezVousDTO rdvDto = rendezVousService.getRendezVousDTOByToken(token);
            return ResponseEntity.ok(rdvDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Associer une candidature à un rendez-vous
    @PutMapping("/{rdvId}/candidature/{candidatureId}")
    public ResponseEntity<?> associerCandidature(
        @PathVariable Long rdvId,
        @PathVariable Long candidatureId) {
        try {
            RendezVous rdv = rendezVousService.associerCandidature(rdvId, candidatureId);
            return ResponseEntity.ok(rdv);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Récupérer la liste des rendez-vous planifiés avec candidature entretien planifié
    @GetMapping("/entretien-planifie")
    public ResponseEntity<List<RendezVousDTO>> getRendezVousAvecEntretienPlanifie() {
        List<RendezVousDTO> list = rendezVousService.getRendezVousPlanifiesAvecCandidature();
        return ResponseEntity.ok(list);
    }

    // Modifier le statut d’un rendez-vous
    @PutMapping("/{id}/statut")
    public ResponseEntity<?> modifierStatut(
        @PathVariable Long id,
        @RequestBody Map<String, String> body) {
        try {
            String nouveauStatut = body.get("statut");
            if (nouveauStatut == null || nouveauStatut.isEmpty()) {
                return ResponseEntity.badRequest().body("Le statut ne peut pas être vide");
            }

            RendezVous rdvModifie = rendezVousService.modifierStatut(id, nouveauStatut);
            return ResponseEntity.ok(rdvModifie);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Mise à jour complète via DTO
    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateRendezVousFromDTO(@PathVariable Long id, @RequestBody RendezVousDTO dto) {
        try {
            RendezVous updated = rendezVousService.updateRendezVousFromDTO(id, dto);
            RendezVousDTO updatedDto = new RendezVousDTO(updated);
            if (updated.getCandidature() != null) {
                Candidature c = updated.getCandidature();
                String nomOffre = c.getOffre() != null ? c.getOffre().getTitre() : null;
                updatedDto.setCandidature(new CandidatureSummaryDTO(
                    c.getId(),
                    c.getCandidat().getFirstName(),
                    c.getCandidat().getLastName(),
                    c.getCandidat().getEmail(),
                    c.getImageUrl(),
                    nomOffre
                ));
            }
            return ResponseEntity.ok(updatedDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Supprimer un rendez-vous par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRendezVous(@PathVariable Long id) {
        try {
            rendezVousService.supprimerRendezVous(id);
            return ResponseEntity.ok().body("Rendez-vous supprimé avec succès.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Envoi d'email pour informer du rendez-vous planifié avec lien complet dans le corps HTML
    @PostMapping("/send-email-entretien")
    public ResponseEntity<?> envoyerEmailEntretien(@RequestBody EmailEntretienRequest request) {
        logger.info("Requête d'envoi email entretien reçue : email={}, dateHeure={}, lienVisio={}",
            request.getEmail(), request.getDateHeure(), request.getLienVisio());

        try {
            if (request.getEmail() == null || request.getDateHeure() == null || request.getLienVisio() == null) {
                logger.warn("Paramètres manquants dans la requête d'envoi email entretien");
                return ResponseEntity.badRequest().body("Paramètres manquants.");
            }

            // Appel méthode texte simple qui envoie le lien complet avec token
            emailService.envoyerInvitationEntretien(
                request.getEmail(),
                request.getParsedDateHeure(),
                request.getLienVisio()
            );

            logger.info("Email entretien envoyé avec succès à {}", request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Email envoyé avec succès."));
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email entretien", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de l'envoi de l'email : " + e.getMessage()));
        }
    }

    // Envoi d'email générique
    @PostMapping("/send-email")
    public ResponseEntity<?> envoyerEmailGeneric(@RequestBody Map<String, String> payload) {
        try {
            String to = payload.get("to");
            String subject = payload.get("subject");
            String body = payload.get("body");
            if (to == null || subject == null || body == null) {
                return ResponseEntity.badRequest().body("Paramètres manquants.");
            }
            emailService.envoyerEmail(to, subject, body);
            return ResponseEntity.ok("Email envoyé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

}
