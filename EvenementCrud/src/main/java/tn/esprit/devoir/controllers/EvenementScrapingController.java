package tn.esprit.devoir.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.service.IEvenementService;
import tn.esprit.devoir.service.ScraperService;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")  // Autoriser les requêtes cross-origin si tu appelles depuis un frontend distant
@RestController
@RequestMapping("/api/evenements/scraping")
@AllArgsConstructor
public class EvenementScrapingController {

    private final IEvenementService iEvenementService;
    private final ScraperService scraperService;  // Injection du service scraper


    // Cette méthode récupère uniquement les événements à venir dont la source est "scraping"
    @GetMapping("/avenir")
    public ResponseEntity<List<Evenement>> getEvenementsAvenirScraping() {
        List<Evenement> events = iEvenementService.getEvenementsAvenirBySource("scraping");
        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(events);
    }



    @PostMapping("/auto")
    public ResponseEntity<?> addOrUpdateScrapedEvent(@RequestBody Evenement evenement) {
        if (evenement.getCapacity() <= 0) {
            evenement.setCapacity(100);
        }
        if (evenement.getDatedebut() == null) {
            evenement.setDatedebut(LocalDate.now());
        }
        if (evenement.getDatefin() == null) {
            evenement.setDatefin(evenement.getDatedebut());
        }

        evenement.setStarRating(0);
        evenement.setSource("scraping");

        Evenement saved = iEvenementService.addOrUpdateScrapedEvent(evenement);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/all-scraping")
    public ResponseEntity<List<Evenement>> getAllScrapedEvents() {
        List<Evenement> events = iEvenementService.getEvenementsBySource("scraping");

        if (events == null || events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(events);
    }


    @DeleteMapping("/delete-all-scraping")
    public ResponseEntity<String> deleteAllScrapedEvents() {
        iEvenementService.deleteBySource("scraping");
        return ResponseEntity.ok("Tous les événements scrappés ont été supprimés.");
    }


    @DeleteMapping("/clear")
    public ResponseEntity<String> clearScrapedEvents() {
        try {
            iEvenementService.deleteAllBySource("scraping");
            return ResponseEntity.ok("Événements scrappés supprimés avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression des événements scrappés.");
        }
    }


    @PostMapping("/scrape-now")
    public ResponseEntity<String> scrapeNow() {
        // Appelle ton service scraper qui va récupérer les événements externes et appeler addOrUpdateScrapedEvent
        scraperService.scrapeAndSaveEvents();
        return ResponseEntity.ok("Scraping lancé avec succès");
    }



}
