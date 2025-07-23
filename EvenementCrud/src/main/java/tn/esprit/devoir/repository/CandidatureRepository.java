package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.devoir.entite.Candidature;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findByCandidatId(Long candidatId);

    List<Candidature> findByOffreId(Long offreId);

    List<Candidature> findAll();

    // Ajout d'une annotation @Query avec JPQL pour cette méthode
    @Query("SELECT c FROM Candidature c LEFT JOIN FETCH c.candidat")
    List<Candidature> findAllWithCandidat();

    List<Candidature> findAllByOrderByDateCandidatureDesc();

    boolean existsByCandidatIdAndOffreId(Long candidatId, Long offreId);

    //   List<Candidature> findByUserId(Long userId);
}
