package tn.esprit.devoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.entite.React;
import tn.esprit.devoir.repository.IReact;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactServiceImp implements ReactService {

    @Autowired
    private IReact iReact;

    @Autowired
    private ActualiteService actualiteService;

    @Override
    public React AddReact(React react) {
        return iReact.save(react);
    }

    @Override
    public void add(long idActualite, String username, Boolean statut) {
        React existingReact = iReact.findReactByActualite_IdActualiteAndUsername(idActualite, username);

        if (existingReact == null) {
            React newReact = new React(actualiteService.findById(idActualite), username, statut);
            iReact.save(newReact);
        } else if (existingReact.isReactionType() == statut) {
            // même réaction → suppression
            iReact.deleteById(existingReact.getId());
        } else {
            // changer le type de réaction
            existingReact.setReactionType(statut);
            iReact.save(existingReact);
        }
    }

    @Override
    public React findById(Long id) {
        return iReact.findById(id).orElse(null);
    }

    @Override
    public void deleteCommentaireById(Long id) {
        iReact.deleteById(id);
    }

    @Override
    public Map<String, Long> getReactionCounts(Long idAct) {
        long likes = iReact.countByActualite_IdActualiteAndReactionType(idAct, true);
        long dislikes = iReact.countByActualite_IdActualiteAndReactionType(idAct, false);

        Map<String, Long> counts = new HashMap<>();
        counts.put("likes", likes);
        counts.put("dislikes", dislikes);

        return counts;
    }

    @Override
    public Boolean getUserReaction(Long actualiteId, String username) {
        Optional<React> reaction = Optional.ofNullable(iReact.findReactByActualite_IdActualiteAndUsername(actualiteId, username));
        return reaction.map(React::isReactionType).orElse(null);
    }

}
