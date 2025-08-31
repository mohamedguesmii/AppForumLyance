package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.devoir.entite.Actualite;

import java.util.List;

@Repository
public interface IActualite extends JpaRepository<Actualite, Long> {

    // Méthode pour récupérer la dernière actualité avec reacts = true
    @Query("SELECT a FROM Actualite a WHERE a.reacts = true ORDER BY a.id DESC")
    Actualite findTopActiveActualite();

    // Exemple : récupérer toutes les actualités actives
    @Query("SELECT a FROM Actualite a WHERE a.reacts = true ORDER BY a.id DESC")
    List<Actualite> findAllActiveActualites();
}
