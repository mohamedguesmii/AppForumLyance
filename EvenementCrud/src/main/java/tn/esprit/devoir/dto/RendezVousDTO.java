package tn.esprit.devoir.dto;

import tn.esprit.devoir.entite.Offre;
import tn.esprit.devoir.entite.RendezVous;
import tn.esprit.devoir.entite.TypeRendezVous;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class RendezVousDTO {

    private Long id;
    private LocalDateTime dateHeure;
    private String tokenAcces;
    private Boolean actif;
    private String statut;
    private Long candidatureId;

    private String prenom;
    private String nom;
    private String email;
    private String imageUrl;

    private TypeRendezVous type;
    private String lienVisio;

    private CandidatureSummaryDTO candidature;

    public RendezVousDTO() {}

    // Constructeur à partir de l'entité RendezVous
    public RendezVousDTO(RendezVous rdv) {
        this.id = rdv.getId();
        this.dateHeure = rdv.getDateHeure();
        this.tokenAcces = rdv.getTokenAcces();
        this.actif = rdv.getActif();
        this.statut = rdv.getStatut() != null ? rdv.getStatut().name() : null;

        if (rdv.getCandidature() != null) {
            this.candidatureId = rdv.getCandidature().getId();
            this.email = rdv.getCandidature().getEmail();
            this.imageUrl = rdv.getCandidature().getImageUrl();

            if (rdv.getCandidature().getCandidat() != null) {
                this.prenom = rdv.getCandidature().getCandidat().getFirstName();
                this.nom = rdv.getCandidature().getCandidat().getLastName();

                String nomOffre = null;
                Offre offre = rdv.getCandidature().getOffre();
                if (offre != null) {
                    nomOffre = offre.getTitre();
                }

                this.candidature = new CandidatureSummaryDTO(
                    rdv.getCandidature().getId(),
                    this.prenom,
                    this.nom,
                    this.email,
                    this.imageUrl,
                    nomOffre
                );
            }
        }

        this.type = rdv.getType();
        this.lienVisio = rdv.getLienVisio();
    }

    // === Getters et Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getTokenAcces() { return tokenAcces; }
    public void setTokenAcces(String tokenAcces) { this.tokenAcces = tokenAcces; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getCandidatureId() { return candidatureId; }
    public void setCandidatureId(Long candidatureId) { this.candidatureId = candidatureId; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public CandidatureSummaryDTO getCandidature() { return candidature; }
    public void setCandidature(CandidatureSummaryDTO candidature) { this.candidature = candidature; }

    public TypeRendezVous getType() { return type; }
    public void setType(TypeRendezVous type) { this.type = type; }

    public String getLienVisio() { return lienVisio; }
    public void setLienVisio(String lienVisio) { this.lienVisio = lienVisio; }

    // Méthode utilitaire pour préparer un payload simplifié pour le webhook n8n
    public Map<String, String> toWebhookPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("nom", this.prenom != null ? this.prenom : "");
        payload.put("email", this.email != null ? this.email : "");
        payload.put("dateHeure", this.dateHeure != null ? this.dateHeure.toString() : "");
        payload.put("lienMeet", this.lienVisio != null ? this.lienVisio : "");
        return payload;
    }
}
