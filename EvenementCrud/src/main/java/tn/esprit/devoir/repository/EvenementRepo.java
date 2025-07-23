package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.devoir.entite.Evenement;

import java.time.LocalDate;
import java.util.List;

public interface EvenementRepo extends JpaRepository<Evenement, Long> {

    // Récupérer tous les événements dont la date de début est aujourd'hui ou après (à venir)
    List<Evenement> findByDatedebutGreaterThanEqual(LocalDate date);

    // Vérifier l'existence d'un événement par titre et date
    boolean existsByTitleAndDatedebut(String title, LocalDate datedebut);

    // Récupérer événements scrappés à venir, triés par date croissante
    List<Evenement> findBySourceAndDatedebutGreaterThanEqualOrderByDatedebutAsc(String source, LocalDate datedebut);

    // Récupérer tous les événements par source
    List<Evenement> findBySource(String source);

    // Supprimer tous les événements par source
    @Modifying
    @Transactional
    @Query("DELETE FROM Evenement e WHERE e.source = :source")
    void deleteBySource(@Param("source") String source);
}
