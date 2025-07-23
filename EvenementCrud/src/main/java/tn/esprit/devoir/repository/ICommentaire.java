package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.devoir.entite.Commentaire;

import java.util.List;

@Repository
public interface ICommentaire extends JpaRepository<Commentaire,Long> {
    List<Commentaire> findCommentaireByActualite_IdActualite(long id) ;
}
