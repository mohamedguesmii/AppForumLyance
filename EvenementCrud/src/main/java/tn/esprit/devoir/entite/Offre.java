package tn.esprit.devoir.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "offres")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String domaine;
    private LocalDate datePublication;
    private String type;
    private String duree;

    @Enumerated(EnumType.STRING)
    private StatutOffre statut = StatutOffre.EN_ATTENTE;

    // Champ transient pour JSON
    @Transient
    private Long evenementId;

    // Relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id")
    @JsonIgnore
    private Evenement evenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    @JsonIgnore
    private User createur;

    // Constructeurs

    public Offre() {}

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }

    public StatutOffre getStatut() { return statut; }
    public void setStatut(StatutOffre statut) { this.statut = statut; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }

    public User getCreateur() { return createur; }
    public void setCreateur(User createur) { this.createur = createur; }

    // Gestion manuelle de evenementId pour JSON

    public Long getEvenementId() {
        return (evenement != null) ? evenement.getIdevent() : null;
    }

    public void setEvenementId(Long evenementId) {
        this.evenementId = evenementId;
        if (evenementId != null) {
            Evenement e = new Evenement();
            e.setIdevent(evenementId);
            this.evenement = e;
        } else {
            this.evenement = null;
        }
    }

    public Integer getCreateurId() {
        return (createur != null) ? createur.getId() : null;
    }

    public void setCreateurId(Long createurId) {
        if (createurId != null) {
            User u = new User();
            u.setId(Math.toIntExact(createurId));
            this.createur = u;
        } else {
            this.createur = null;
        }
    }

    // Expose le titre de l'événement dans le JSON

    @JsonProperty("evenementTitre")
    public String getEvenementTitre() {
        return (evenement != null) ? evenement.getTitle() : null;
    }
}
