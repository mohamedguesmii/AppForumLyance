package tn.esprit.devoir.service;

import tn.esprit.devoir.entite.React;
import java.util.Map;

public interface ReactService {
    React AddReact(React react);

    void add(long idActualite, String username, Boolean statut);

    React findById(Long id);

    void deleteCommentaireById(Long id);

    Map<String, Long> getReactionCounts(Long idAct);

    Boolean getUserReaction(Long idAct, String username);
}
