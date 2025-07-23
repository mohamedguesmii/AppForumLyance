package tn.esprit.devoir.service;

import tn.esprit.devoir.entite.Actualite;

import java.util.List;

public interface ActualiteService {

    public Actualite addActualite(Actualite actualite);

    public Actualite updateActualite(Actualite actualite , Long idActualite);

    public Actualite findById( Long idActualite);

    public List<Actualite> retrieveAllActualite();

    public void deleteActualiteById( Long idActualite);

    Actualite Best();
}
