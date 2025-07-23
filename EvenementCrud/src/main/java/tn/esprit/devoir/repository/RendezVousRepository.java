package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.devoir.entite.Candidature;
import tn.esprit.devoir.entite.RendezVous;
import tn.esprit.devoir.entite.StatutCandidature;
import tn.esprit.devoir.entite.StatutRendezVous;

import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    Optional<RendezVous> findByTokenAcces(String token);

    List<RendezVous> findByStatutAndCandidature_Statut(StatutRendezVous statutRdv, StatutCandidature statutCandidature);

    boolean existsByCandidature(Candidature candidature);


}

