package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.entite.Actualite;
import tn.esprit.devoir.entite.Commentaire;
import tn.esprit.devoir.service.CommentaireService;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/Commentaires")
public class CommentaireController {
    @Autowired
    private CommentaireService commentaireService;

    @GetMapping("/all/{idActualite}")
    public List<Commentaire>retrieveAllCommentaireByActualiteId(@PathVariable("idActualite") long id) {
        return commentaireService.retrieveAllCommentaireByActualiteId(id);
    }

    @GetMapping("/{id}")
    public Commentaire findById(@PathVariable Long id) {
        return commentaireService.findById(id);
    }

    @PostMapping("/add/{idAct}")
    public List<Actualite> addCommentaire(@RequestBody Commentaire commentaire, @PathVariable("idAct") long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // ← le nom de l'utilisateur connecté

        commentaire.setUsername(username);            // on l'ajoute au commentaire
        commentaire.setDate(LocalDateTime.now());     // on ajoute la date si tu veux
        commentaire.setModifiee(false);               // par défaut non modifié

        return commentaireService.AddCommentaire(commentaire, id);
    }




    @PutMapping("/update/{id}")
    public Commentaire updateCommentaire(@RequestBody Commentaire commentaire, @PathVariable("id") Long id) {
        return commentaireService.updateCommentaire(commentaire, id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteCommentaireById(@PathVariable Long id) {
        commentaireService.deleteCommentaireById(id);
    }


}
