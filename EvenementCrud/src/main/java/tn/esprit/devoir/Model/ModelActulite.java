package tn.esprit.devoir.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelActulite {
    private String actuality;
    private String url;
    private String description;  // <-- déclaration du champ description

    // Avec Lombok, pas besoin d'écrire explicitement les getters/setters
    // sauf si tu veux une logique spécifique


}
