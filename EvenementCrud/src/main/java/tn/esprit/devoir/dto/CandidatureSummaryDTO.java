package tn.esprit.devoir.dto;

public class CandidatureSummaryDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String imageUrl;
    private String nomOffre;  // <- nouveau champ

    public CandidatureSummaryDTO() {}

    // Constructeur avec nomOffre ajouté
    public CandidatureSummaryDTO(Long id, String nom, String prenom, String email, String imageUrl, String nomOffre) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.imageUrl = imageUrl;
        this.nomOffre = nomOffre;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNomOffre() {
        return nomOffre;
    }

    public void setNomOffre(String nomOffre) {
        this.nomOffre = nomOffre;
    }
}
