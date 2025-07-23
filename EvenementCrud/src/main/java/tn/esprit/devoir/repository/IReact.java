package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.devoir.entite.React;

public interface IReact extends JpaRepository<React, Long> {

    React findReactByActualite_IdActualiteAndUsername(long id, String username);

    long countByActualite_IdActualiteAndReactionType(Long idAct, boolean reactionType);
    React findReactByActualite_IdActualiteAndUsername(Long actualiteId, String username);

}
