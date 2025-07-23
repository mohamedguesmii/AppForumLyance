package tn.esprit.devoir.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.entite.Evenement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ScraperService {

    private final IEvenementService evenementService;

    /**
     * Simule un scraping d'événements externes, puis les ajoute ou met à jour en base.
     */
    public void scrapeAndSaveEvents() {
        List<Evenement> scrapedEvents = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            Evenement ev = new Evenement();
            ev.setTitle("Événement scrappé n°" + i);
            ev.setDescription("Description détaillée de l'événement scrappé numéro " + i);
            ev.setDatedebut(LocalDate.now().plusDays(i * 3));
            ev.setDatefin(LocalDate.now().plusDays(i * 3 + 1));
            ev.setAdresse("Ville " + i);
            ev.setCapacity(100 + i * 20);
            ev.setSource("scraping");
            ev.setStarRating(0);
            ev.setScraped(true);
            // Ajout de l'image - exemple d'URL d'image publique (tu peux changer par ta propre URL)
            ev.setImageUrl("https://picsum.photos/seed/event" + i + "/300/200");
            scrapedEvents.add(ev);
        }

        for (Evenement ev : scrapedEvents) {
            evenementService.addOrUpdateScrapedEvent(ev);
        }
    }

}
