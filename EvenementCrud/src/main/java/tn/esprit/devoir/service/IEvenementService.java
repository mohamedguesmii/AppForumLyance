package tn.esprit.devoir.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.entite.Evenement;

import java.time.LocalDate;
import java.util.List;

public interface IEvenementService {

    Evenement handleImageFileUpload(MultipartFile fileImage, long id);

    Evenement addEvenement(Evenement evenement);

    Evenement updateEvenement(Evenement evenement);

    void deleteEvenement(Long id);

    Evenement updateRating(Long id, int newRating);

    boolean existsByTitleAndDatedebut(String title, LocalDate datedebut);

    List<Evenement> getEvenementsAvenirBySource(String source);

    Evenement getEvenementById(Long id);

    List<Evenement> getAllEvenements();

    List<Evenement> getEvenementsByTitle(String title);

    List<Evenement> getEvenementsAvenir();

    void deleteAllBySource(String source);

    List<Evenement> getEvenementsBySource(String source);

    void deleteBySource(String source);

    Evenement addOrUpdateScrapedEvent(Evenement evenement);
}
