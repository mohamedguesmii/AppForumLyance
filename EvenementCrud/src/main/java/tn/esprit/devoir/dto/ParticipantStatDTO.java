package tn.esprit.devoir.dto;

public class ParticipantStatDTO {
    private String evenement;
    private String role; // rôle sous forme de texte (ex: "ADMINISTRATEUR")
    private Long count;

    // Constructeur utilisé dans la requête JPQL
    public ParticipantStatDTO(String evenement, String role, Long count) {
        this.evenement = evenement;
        this.role = role;
        this.count = count;
    }

    // Getters & Setters
    public String getEvenement() {
        return evenement;
    }

    public void setEvenement(String evenement) {
        this.evenement = evenement;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    // Alias pour affichage/compatibilité
    public String getRoleName() {
        return role;
    }
}
