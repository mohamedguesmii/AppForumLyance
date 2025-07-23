package tn.esprit.devoir.controllers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.Cloudinary.CloudinaryService;
import tn.esprit.devoir.Model.ModelUrl;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.service.IEvenementService;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/evenements")
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvenementController {

    IEvenementService iEvenementService;
    CloudinaryService cloudinaryService;


    // ✅ Get all events
    @GetMapping("/all")
    public ResponseEntity<List<Evenement>> getAllEvenements() {
        log.info("Fetching all events");
        List<Evenement> evenements = iEvenementService.getAllEvenements();
        if (evenements.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(evenements);
    }

    // ✅ Get event by ID
    @GetMapping("/{idevent:[0-9]+}")
    public ResponseEntity<Evenement> getEvenementById(@PathVariable("idevent") Long idevent) {
        log.info("Fetching event by id: {}", idevent);
        Evenement event = iEvenementService.getEvenementById(idevent);
        if (event == null) {
            log.warn("Event not found with id: {}", idevent);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    // ✅ Get events by title
    @GetMapping("/by-title/{title}")
    public ResponseEntity<List<Evenement>> getEvenementsByTitle(@PathVariable String title) {
        log.info("Fetching events by title containing: {}", title);
        List<Evenement> events = iEvenementService.getEvenementsByTitle(title);
        if (events == null || events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(events);
    }

    // ✅ Add new event
    @PostMapping
    public ResponseEntity<?> addEvenement(
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("capacity") long capacity,
        @RequestParam("datedebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datedebut,
        @RequestParam("datefin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datefin,
        @RequestParam("adresse") String adresse,
        @RequestParam(value = "fileImage", required = false) MultipartFile fileImage
    ) {
        try {
            if (datedebut.isAfter(datefin)) {
                return ResponseEntity.badRequest().body("La date de début doit être avant la date de fin.");
            }
            if (capacity <= 0) {
                return ResponseEntity.badRequest().body("La capacité doit être positive.");
            }

            Evenement e = new Evenement();
            e.setTitle(title);
            e.setDescription(description);
            e.setCapacity(capacity);
            e.setDatedebut(datedebut);
            e.setDatefin(datefin);
            e.setAdresse(adresse);
            e.setStarRating(0);

            if (fileImage != null && !fileImage.isEmpty()) {
                try {
                    String url = cloudinaryService.uploadFile(fileImage, "folder_1");
                    e.setImageUrl(url);
                } catch (Exception ex) {
                    log.error("Erreur upload image", ex);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload de l'image");
                }
            }

            Evenement saved = iEvenementService.addEvenement(e);

            // ✅ Correction ici :

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception ex) {
            log.error("Erreur lors de l'ajout de l'événement", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
        }
    }


    // ✅ Update event
    @PutMapping("/{idevent}")
    public ResponseEntity<?> updateEvenement(
        @PathVariable("idevent") Long idevent,
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("capacity") Integer capacity,
        @RequestParam("datedebut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datedebut,
        @RequestParam("datefin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datefin,
        @RequestParam("adresse") String adresse,
        @RequestParam(value = "fileImage", required = false) MultipartFile fileImage
    ) {
        Evenement existing = iEvenementService.getEvenementById(idevent);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setTitle(title);
        existing.setDescription(description);
        existing.setCapacity(capacity);
        existing.setDatedebut(datedebut);
        existing.setDatefin(datefin);
        existing.setAdresse(adresse);

        if (fileImage != null && !fileImage.isEmpty()) {
            // traitement upload image
            String urlImage = cloudinaryService.uploadFile(fileImage, "events");
            existing.setImageUrl(urlImage);
        }

        Evenement updated = iEvenementService.updateEvenement(existing);
        return ResponseEntity.ok(updated);
    }


    // ✅ Delete event
    @DeleteMapping("/{idevent}")
    public ResponseEntity<Void> deleteEvenement(@PathVariable("idevent") Long idevent) {
        log.info("Deleting event with id: {}", idevent);
        Evenement existing = iEvenementService.getEvenementById(idevent);
        if (existing == null) {
            log.warn("Event not found with id: {}", idevent);
            return ResponseEntity.notFound().build();
        }
        iEvenementService.deleteEvenement(idevent);
        return ResponseEntity.noContent().build();
    }

    // ✅ Upload file (generic)
    @PostMapping("/upload")
    public ResponseEntity<ModelUrl> upload(@RequestParam("file") MultipartFile file) {
        log.info("Uploading file to Cloudinary");
        String url = cloudinaryService.uploadFile(file, "folder_1");
        ModelUrl m = new ModelUrl();
        m.setUrl(url);
        return ResponseEntity.ok(m);
    }

    // ✅ Update rating
    @PutMapping("/rating/{idevent}")
    public ResponseEntity<?> updateRating(@PathVariable Long idevent, @RequestBody Evenement updatedEvaluation) {
        log.info("Updating rating for event id: {}", idevent);
        Evenement evaluation = iEvenementService.updateRating(idevent, updatedEvaluation.getStarRating());
        if (evaluation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(evaluation);
    }

    // ✅ Upload image for event
    @PostMapping("/upload-image/{idevent}")
    public ResponseEntity<?> handleImageFileUpload(@RequestParam("fileImage") MultipartFile fileImage, @PathVariable long idevent) {
        log.info("Uploading image for event id: {}", idevent);
        Evenement updatedEvent = iEvenementService.handleImageFileUpload(fileImage, idevent);
        if (updatedEvent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedEvent);
    }




}
