package tn.esprit.devoir.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure;

    private String tokenAcces;

    private Boolean actif = true;

    private String lienVisio;

    @Enumerated(EnumType.STRING)
    private TypeRendezVous type = TypeRendezVous.PRESENTIEL;

    @Enumerated(EnumType.STRING)
    private StatutRendezVous statut = StatutRendezVous.PLANIFIE;

    @OneToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Candidature candidature;

    // Getters & setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getTokenAcces() {
        return tokenAcces;
    }

    public void setTokenAcces(String tokenAcces) {
        this.tokenAcces = tokenAcces;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public StatutRendezVous getStatut() {
        return statut;
    }

    public void setStatut(StatutRendezVous statut) {
        this.statut = statut;
    }

    public Candidature getCandidature() {
        return candidature;
    }

    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;
    }

    public String getLienVisio() {
        return lienVisio;
    }

    public void setLienVisio(String lienVisio) {
        this.lienVisio = lienVisio;
    }

    public TypeRendezVous getType() {
        return type;
    }

    public void setType(TypeRendezVous type) {
        this.type = type;
    }
}
