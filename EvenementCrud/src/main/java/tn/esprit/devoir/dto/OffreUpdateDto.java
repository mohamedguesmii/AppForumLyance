package tn.esprit.devoir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OffreUpdateDto {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(min = 2, max = 100, message = "Le lieu doit contenir entre 2 et 100 caractères")
    private String lieu;

    // Si tu veux gérer domaine, type, duree
    private String domaine;
    private String type;
    private String duree;

    // Champ statut, acceptée/refusée/en attente
    @Pattern(regexp = "acceptée|refusée|en attente", flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Le statut doit être 'acceptée', 'refusée' ou 'en attente'")
    private String statut;


    private Long evenementId; // Ajoute aussi si tu veux gérer la liaison avec l'événement
}
