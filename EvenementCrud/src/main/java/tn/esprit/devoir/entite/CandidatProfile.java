package tn.esprit.devoir.entite;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "candidat_profiles")
public class CandidatProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String domaine;

    private String typeRecherche; // ex : "Stage" ou "Emploi"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidat_competences", joinColumns = @JoinColumn(name = "candidat_profile_id"))
    @Column(name = "competence")
    private List<String> competences;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomaine() {
        return domaine;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    public String getTypeRecherche() {
        return typeRecherche;
    }

    public void setTypeRecherche(String typeRecherche) {
        this.typeRecherche = typeRecherche;
    }

    public List<String> getCompetences() {
        return competences;
    }

    public void setCompetences(List<String> competences) {
        this.competences = competences;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
