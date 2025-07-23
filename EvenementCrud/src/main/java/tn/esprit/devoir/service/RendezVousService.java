package tn.esprit.devoir.service;

import tn.esprit.devoir.dto.RendezVousDTO;
import tn.esprit.devoir.entite.RendezVous;

import java.util.List;

public interface RendezVousService {

    RendezVous getRendezVousByToken(String token);

    RendezVous creerRendezVous(RendezVous rdv);

    RendezVous associerCandidature(Long rdvId, Long candidatureId);

    RendezVousDTO getRendezVousDTOByToken(String token);


    public List<RendezVousDTO> getRendezVousPlanifiesAvecCandidature();

    RendezVous modifierStatut(Long id, String nouveauStatut);

    RendezVous updateRendezVousFromDTO(Long id, RendezVousDTO dto);

    void supprimerRendezVous(Long id);
}
