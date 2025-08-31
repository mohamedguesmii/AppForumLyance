package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.devoir.entite.Actualite;

@Repository
public interface IActualite extends JpaRepository<Actualite, Long> {

    // Si reacts est un boolean
    // Actualite findTopByReactsIsTrue();

    // Si reacts est une collection (ex: List reacts)
    @Query("SELECT a FROM Actualite a WHERE SIZE(a.reacts) > 0 ORDER BY a.id DESC")
    Actualite findTopWithReacts();
}
