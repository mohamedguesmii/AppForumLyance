package tn.esprit.devoir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OffreDto {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 1, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotBlank(message = "Le domaine est obligatoire")
    @Size(min = 2, max = 100, message = "Le domaine doit contenir entre 2 et 50 caractères")
    private String domaine;



    @NotBlank(message = "La date de publication est obligatoire")
    private String datePublication;

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    private String duree;

    private String statut; // pas besoin de validation ici



    @NotNull(message = "L'ID du créateur est obligatoire")
    private Long createurId;

    @NotNull(message = "L'ID de l'événement est obligatoire")
    private Long evenementId;



}
