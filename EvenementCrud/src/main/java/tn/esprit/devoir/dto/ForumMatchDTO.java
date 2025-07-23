package tn.esprit.devoir.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.esprit.devoir.entite.Evenement;

@Data
@AllArgsConstructor
public class ForumMatchDTO {
    private Evenement evenement;
    private int score;
}
