package tn.esprit.devoir.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.OffreDto;
import tn.esprit.devoir.dto.OffreUpdateDto;
import tn.esprit.devoir.entite.Offre;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.repository.EvenementRepo;
import tn.esprit.devoir.repository.UserRepository;
import tn.esprit.devoir.service.CamundaService;
import tn.esprit.devoir.service.OffreServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "http://localhost:4200")
public class OffreController {

    @Autowired
    private OffreServiceImpl offreService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EvenementRepo evenementRepository;

    @Autowired
    private CamundaService camundaService;

    // Création d'une offre + démarrage du process Camunda
    @PostMapping
    public ResponseEntity<?> createOffre(@Valid @RequestBody OffreDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body("❌ " + errorMessage);
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication.getPrincipal() instanceof UserDetails) ?
                ((UserDetails) authentication.getPrincipal()).getUsername() :
                authentication.getPrincipal().toString();

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur connecté introuvable"));

            if (dto.getEvenementId() != null && !evenementRepository.existsById(dto.getEvenementId())) {
                return ResponseEntity.badRequest().body("ID événement invalide ou introuvable");
            }

            Offre offre = new Offre();
            offre.setTitre(dto.getTitre());
            offre.setDescription(dto.getDescription());
            offre.setDomaine(dto.getDomaine());
            offre.setType(dto.getType());
            offre.setDuree(dto.getDuree());
            offre.setDatePublication(LocalDate.now());

            // Utiliser la méthode qui ajoute et démarre le process Camunda
            Offre saved = offreService.ajouterOffreEtDemarrerProcess(offre, user.getId().intValue(), dto.getEvenementId());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("💥 Erreur lors de la création de l'offre : " + e.getMessage());
        }
    }

    // Récupérer toutes les offres
    @GetMapping
    public ResponseEntity<List<Offre>> getAllOffres() {
        List<Offre> offres = offreService.getAllOffres();
        return ResponseEntity.ok(offres);
    }

    // Récupérer offre par id
    @GetMapping("/{id}")
    public ResponseEntity<Offre> getOffreById(@PathVariable Long id) {
        try {
            Offre offre = offreService.getOffreById(id);
            return ResponseEntity.ok(offre);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Supprimer une offre
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long id) {
        offreService.supprimerOffre(id);
        return ResponseEntity.noContent().build();
    }

    // Mettre à jour une offre (avec statut)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffre(@PathVariable Long id, @Valid @RequestBody OffreUpdateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body("❌ " + errorMessage);
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication.getPrincipal() instanceof UserDetails) ?
                ((UserDetails) authentication.getPrincipal()).getUsername() :
                authentication.getPrincipal().toString();

            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur connecté introuvable"));

            Offre updatedOffre = offreService.modifierOffreAvecStatut(id, dto, user.getId().intValue());

            return ResponseEntity.ok(updatedOffre);

        } catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("💥 Erreur lors de la modification de l'offre : " + e.getMessage());
        }
    }

    // --- ENDPOINTS CAMUNDA ---

    // Récupérer les tâches assignées à "rh"
    @GetMapping("/tasks/rh")
    public ResponseEntity<List<Map<String, Object>>> getTasksForRh() {
        List<Map<String, Object>> tasks = camundaService.getTasksForRh();
        return ResponseEntity.ok(tasks);
    }

    // Compléter une tâche Camunda (ex: valider ou refuser)
    @PostMapping("/tasks/complete/{taskId}")
    public ResponseEntity<?> completeTask(
        @PathVariable String taskId,
        @RequestBody Map<String, String> body) {

        String statut = body.getOrDefault("statut", "VALIDÉ"); // Exemple: VALIDÉ, REFUSÉ, EN_ATTENTE, etc.
        camundaService.completeTask(taskId, statut);

        return ResponseEntity.ok(Map.of("message", "Tâche " + taskId + " complétée avec statut " + statut));
    }


    // ✅ Historique des offres validées ou refusées
    @GetMapping("/historique")
    public ResponseEntity<List<OffreDto>> getHistoriqueOffres() {
        List<OffreDto> historique = offreService.getHistoriqueOffres();
        return ResponseEntity.ok(historique);
    }


}
