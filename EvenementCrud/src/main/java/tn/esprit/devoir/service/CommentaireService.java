package tn.esprit.devoir.service;

import tn.esprit.devoir.entite.Actualite;
import tn.esprit.devoir.entite.Commentaire;

import java.util.List;

public interface CommentaireService {
  List<Actualite> AddCommentaire(Commentaire commentaire , long id);


    Commentaire updateCommentaire(Commentaire commentaire, long idCommentaire);

    Commentaire findById(Long idCommentaire);

    List<Commentaire> retrieveAllCommentaireByActualiteId(long idCommentaire);

    void deleteCommentaireById(Long idCommentaire);
}
