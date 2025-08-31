package tn.esprit.devoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.entite.Actualite;
import tn.esprit.devoir.repository.IActualite;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActualiteServiceIMP implements ActualiteService {

    @Autowired
    private IActualite iActualite;

    @Override
    public Actualite addActualite(Actualite actualite) {
        actualite.setDatePub(LocalDateTime.now());
        return iActualite.save(actualite);
    }

    @Override
    public Actualite updateActualite(Actualite actualite, Long idActualite) {
        Actualite actualiteToUpdate = iActualite.findById(idActualite).orElse(null);
        if (actualiteToUpdate != null) {
            actualiteToUpdate.setActuality(actualite.getActuality());
            actualiteToUpdate.setDatePub(LocalDateTime.now());
            return iActualite.save(actualiteToUpdate);
        }
        return null;
    }

    @Override
    public Actualite findById(Long idActualite) {
        return iActualite.findById(idActualite).orElse(null);
    }

    @Override
    public List<Actualite> retrieveAllActualite() {
        return iActualite.findAll();
    }

    @Override
    public void deleteActualiteById(Long idActualite) {
        iActualite.deleteById(idActualite);
    }

    @Override
    public Actualite Best() {
        // Utilisation d'une méthode @Query dans le repository pour gérer une collection
        return iActualite.findTopWithReacts();
    }
}
