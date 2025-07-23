package tn.esprit.devoir.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.repository.EvenementRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EvenementServiceImp implements IEvenementService {

    private final EvenementRepo evenementRepo;
    private final FileStorageService fileStorageService; // supposé déjà implémenté
    private final CamundaService forumWorkflowService;


    @Override
    public Evenement addEvenement(Evenement evenement) {
        log.info("Ajout d’un événement : {}", evenement);
        Evenement saved = evenementRepo.save(evenement);

        // ✅ Correction ici : passer aussi le titre de l'événement
        boolean started = forumWorkflowService.demarrerValidationForum(saved.getIdevent(), saved.getTitle());

        if (started) {
            log.info("✅ Processus Camunda lancé pour l'événement ID = {}", saved.getIdevent());
        } else {
            log.error("❌ Échec du démarrage du processus Camunda pour l'événement ID = {}", saved.getIdevent());
        }

        return saved;
    }


    @Override
    public Evenement updateEvenement(Evenement evenement) {
        return evenementRepo.save(evenement);
    }

    @Override
    public Evenement getEvenementById(Long id) {
        return evenementRepo.findById(id).orElse(null);
    }

    @Override
    public void deleteEvenement(Long id) {
        evenementRepo.deleteById(id);
    }

    @Override
    public List<Evenement> getAllEvenements() {
        return evenementRepo.findAll();
    }

    @Override
    public List<Evenement> getEvenementsByTitle(String title) {
        // Implémenter si besoin
        return null;
    }

    @Override
    public List<Evenement> getEvenementsAvenir() {
        return evenementRepo.findByDatedebutGreaterThanEqual(LocalDate.now());
    }

    @Override
    public List<Evenement> getEvenementsBySource(String source) {
        return evenementRepo.findBySourceAndDatedebutGreaterThanEqualOrderByDatedebutAsc(source, LocalDate.now());
    }

    @Override
    public List<Evenement> getEvenementsAvenirBySource(String source) {
        return getEvenementsBySource(source);
    }

    @Override
    public void deleteAllBySource(String source) {
        evenementRepo.deleteBySource(source);
    }

    @Override
    public void deleteBySource(String source) {
        evenementRepo.deleteBySource(source);
    }

    @Override
    public Evenement addOrUpdateScrapedEvent(Evenement evenement) {
        Optional<Evenement> existingEventOpt = evenementRepo.findAll().stream()
            .filter(ev -> ev.getTitle().equalsIgnoreCase(evenement.getTitle())
                && ev.getDatedebut().isEqual(evenement.getDatedebut()))
            .findFirst();

        if (existingEventOpt.isPresent()) {
            Evenement existing = existingEventOpt.get();
            existing.setDescription(evenement.getDescription());
            existing.setAdresse(evenement.getAdresse());
            existing.setDatefin(evenement.getDatefin());
            existing.setCapacity(evenement.getCapacity());
            existing.setImageUrl(evenement.getImageUrl());
            existing.setSource(evenement.getSource());
            log.info("Mise à jour de l'événement existant : {}", existing.getTitle());
            return evenementRepo.save(existing);
        } else {
            log.info("Ajout d’un nouvel événement scrappé : {}", evenement.getTitle());
            return evenementRepo.save(evenement);
        }
    }

    @Override
    public Evenement updateRating(Long id, int newRating) {
        Optional<Evenement> optionalEvaluation = evenementRepo.findById(id);
        if (optionalEvaluation.isPresent()) {
            Evenement evaluation = optionalEvaluation.get();
            evaluation.setStarRating(newRating);
            return evenementRepo.save(evaluation);
        }
        return null;
    }

    @Override
    public boolean existsByTitleAndDatedebut(String title, LocalDate datedebut) {
        return evenementRepo.existsByTitleAndDatedebut(title, datedebut);
    }

    @Override
    public Evenement handleImageFileUpload(MultipartFile fileImage, long id) {
        if (fileImage.isEmpty()) {
            return null;
        }
        String fileName = fileStorageService.storeFile(fileImage);
        Evenement event = evenementRepo.findById(id).orElse(null);
        if (event != null) {
            event.setImageUrl(fileName);
            return evenementRepo.save(event);
        }
        return null;
    }
}
