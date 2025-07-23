package tn.esprit.devoir.service;

import tn.esprit.devoir.dto.OffreDto;
import tn.esprit.devoir.entite.Offre;

import java.util.List;

public abstract class OffreService {

    public abstract Offre ajouterOffre(Offre offre, Integer createurId, Long evenementId);

    public abstract List<Offre> getAllOffres();

    public abstract Offre getOffreById(Long id);

    public abstract void supprimerOffre(Long id);


    // Ajout de la méthode abstraite pour la modification
// Ajoute cette méthode abstraite pour que l’override soit valide
    public abstract Offre modifierOffre(Long id, OffreDto dto, Integer createurId);
    }
