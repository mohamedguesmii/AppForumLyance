package tn.esprit.devoir.entite;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@DynamicUpdate
@Table(name = "candidatures")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", insertable = false, updatable = false)
    private User candidat;

    @Column(name = "offre_id", nullable = false)
    private Long offreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", insertable = false, updatable = false)
    private Offre offre;

    @Column(name = "date_candidature", nullable = false)
    private LocalDateTime dateCandidature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut;

    @Column(length = 255)
    private String cvUrl;

    @Lob
    @Column(name = "cv_data")
    private byte[] cvData;

    @Column(length = 255)
    private String lettreMotivation;

    @Lob
    @Column(name = "lettre_data")
    private byte[] lettreData;

    @Column(length = 2000)
    private String commentairesRecruteur;

    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(name = "date_miseajour")
    private LocalDateTime dateMiseAJour;

    @Column(name = "date_entretien")
    private LocalDateTime dateEntretien;

    @Column(length = 255)
    private String imageUrl;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    // Nouveaux champs ajoutés pour le score et Camunda
    @Column(nullable = true)
    private Double score;

    @Column(nullable = true)
    private Boolean matched;

    @Column(length = 64)
    private String processInstanceId;

    // === Constructeurs ===

    public Candidature() {
    }

    public Candidature(Long candidatId, Long offreId, LocalDateTime dateCandidature, StatutCandidature statut,
                       String cvUrl, byte[] cvData, String lettreMotivation, byte[] lettreData,
                       String commentairesRecruteur, String telephone, String email,
                       LocalDateTime dateMiseAJour, LocalDateTime dateEntretien,
                       String imageUrl, byte[] imageData) {
        this.candidatId = candidatId;
        this.offreId = offreId;
        this.dateCandidature = dateCandidature;
        this.statut = statut;
        this.cvUrl = cvUrl;
        this.cvData = cvData;
        this.lettreMotivation = lettreMotivation;
        this.lettreData = lettreData;
        this.commentairesRecruteur = commentairesRecruteur;
        this.telephone = telephone;
        this.email = email;
        this.dateMiseAJour = dateMiseAJour;
        this.dateEntretien = dateEntretien;
        this.imageUrl = imageUrl;
        this.imageData = imageData;
    }

    // === Getters & Setters ===

    public Long getId() {
        return id;
    }

    public Long getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(Long candidatId) {
        this.candidatId = candidatId;
    }

    public User getCandidat() {
        return candidat;
    }

    public void setCandidat(User candidat) {
        this.candidat = candidat;
    }

    public Long getOffreId() {
        return offreId;
    }

    public void setOffreId(Long offreId) {
        this.offreId = offreId;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public LocalDateTime getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(LocalDateTime dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public StatutCandidature getStatut() {
        return statut;
    }

    public void setStatut(StatutCandidature statut) {
        this.statut = statut;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl;
    }

    public byte[] getCvData() {
        return cvData;
    }

    public void setCvData(byte[] cvData) {
        this.cvData = cvData;
    }

    public String getLettreMotivation() {
        return lettreMotivation;
    }

    public void setLettreMotivation(String lettreMotivation) {
        this.lettreMotivation = lettreMotivation;
    }

    public byte[] getLettreData() {
        return lettreData;
    }

    public void setLettreData(byte[] lettreData) {
        this.lettreData = lettreData;
    }

    public String getCommentairesRecruteur() {
        return commentairesRecruteur;
    }

    public void setCommentairesRecruteur(String commentairesRecruteur) {
        this.commentairesRecruteur = commentairesRecruteur;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(LocalDateTime dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }

    public LocalDateTime getDateEntretien() {
        return dateEntretien;
    }

    public void setDateEntretien(LocalDateTime dateEntretien) {
        this.dateEntretien = dateEntretien;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getMatched() {
        return matched;
    }

    public void setMatched(Boolean matched) {
        this.matched = matched;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
