package tn.esprit.devoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.entite.Actualite;
import tn.esprit.devoir.entite.Commentaire;
import tn.esprit.devoir.repository.IActualite;
import tn.esprit.devoir.repository.ICommentaire;

import java.time.LocalDateTime;
import java.util.List;
@Service

public class CommentaireServiceImp implements CommentaireService {
    @Autowired
    private ICommentaire iCommentaire;

    @Autowired
    private IActualite iactualite;

    @Override
    public List<Actualite> AddCommentaire(Commentaire commentaire, long id) {
        Actualite actualite = iactualite.findById(id).orElse(null);
        commentaire.setActualite(actualite);
        commentaire.setDate(LocalDateTime.now());
         iCommentaire.save(commentaire);
         return iactualite.findAll();
    }

    @Override
    public Commentaire updateCommentaire(Commentaire commentaire, long idCommentaire) {
        Commentaire commentaire1 = iCommentaire.findById(idCommentaire).orElse(null);
        if (commentaire1 != null) {
            commentaire1.setContenu(commentaire.getContenu());
            commentaire1.setDate(LocalDateTime.now());
            return iCommentaire.save(commentaire1);
        }
        return null;
    }

    @Override
    public Commentaire findById(Long idCommentaire) {
        return iCommentaire.findById(idCommentaire).orElse(null);
    }

    @Override
    public List<Commentaire> retrieveAllCommentaireByActualiteId(long idCommentaire) {
        return iCommentaire.findCommentaireByActualite_IdActualite(idCommentaire);
    }

    @Override
    public void deleteCommentaireById(Long idCommentaire) {
        iCommentaire.deleteById(idCommentaire);
    }
}
