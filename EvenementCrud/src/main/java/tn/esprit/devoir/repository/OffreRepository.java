package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.devoir.entite.Offre;
import tn.esprit.devoir.entite.StatutOffre;
import tn.esprit.devoir.entite.User;

import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long> {

    @Modifying
    @Transactional
    void deleteAllByCreateur(User createur);

    List<Offre> findByCreateur(User user);

    List<Offre> findByEvenementIdevent(Long evenementId);

    List<Offre> findByStatutIn(List<StatutOffre> statuts);


}
