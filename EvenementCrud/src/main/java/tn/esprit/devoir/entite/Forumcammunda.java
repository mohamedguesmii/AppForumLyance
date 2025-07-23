package tn.esprit.devoir.entite;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forumcammunda")
public class Forumcammunda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private String statut;

    private LocalDateTime dateDerniereAction;

    // Constructeurs
    public Forumcammunda() {}

    public Forumcammunda(String titre, String statut, LocalDateTime dateDerniereAction) {
        this.titre = titre;
        this.statut = statut;
        this.dateDerniereAction = dateDerniereAction;
    }

    // Getters et setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateDerniereAction() {
        return dateDerniereAction;
    }

    public void setDateDerniereAction(LocalDateTime dateDerniereAction) {
        this.dateDerniereAction = dateDerniereAction;
    }
}
