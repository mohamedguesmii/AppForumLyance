package tn.esprit.devoir.dto;

import tn.esprit.devoir.entite.StatutCandidature;
import java.time.LocalDateTime;

public class CandidatureDto {

    private Long id;
    private Long candidatId;
    private Long offreId;
    private LocalDateTime dateCandidature;
    private StatutCandidature statut;
    private String cvUrl;
    private String lettreMotivation;
    private String commentairesRecruteur;
    private String telephone;
    private String email;
    private LocalDateTime dateMiseAJour;
    private LocalDateTime dateEntretien;
    private String imageProfilUrl;

    private String titreOffre;
    private String typeOffre;


    // Constructeur vide (requis par certains frameworks)
    public CandidatureDto() {
    }

    // Constructeur depuis entité (optionnel, pratique pour conversion)
    public CandidatureDto(tn.esprit.devoir.entite.Candidature c) {
        this.id = c.getId();
        this.candidatId = (c.getCandidatId() != null) ? Long.valueOf(c.getCandidatId()) : null;
        this.offreId = c.getOffreId();
        this.dateCandidature = c.getDateCandidature();
        this.statut = c.getStatut();
        this.cvUrl = c.getCvUrl();
        this.lettreMotivation = c.getLettreMotivation();
        this.commentairesRecruteur = c.getCommentairesRecruteur();
        this.telephone = c.getTelephone();
        this.email = c.getEmail();
        this.dateMiseAJour = c.getDateMiseAJour();
        this.dateEntretien = c.getDateEntretien();
        // imageProfilUrl sera ajouté via le service

        if (c.getOffre() != null) {
            this.titreOffre = c.getOffre().getTitre();
            this.typeOffre = c.getOffre().getType();
        }
    }

    // Getters et setters

    public String getTitreOffre() {
        return titreOffre;
    }

    public void setTitreOffre(String titreOffre) {
        this.titreOffre = titreOffre;
    }

    public String getTypeOffre() {
        return typeOffre;
    }

    public void setTypeOffre(String typeOffre) {
        this.typeOffre = typeOffre;
    }




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(Long candidatId) {
        this.candidatId = candidatId;
    }

    public Long getOffreId() {
        return offreId;
    }

    public void setOffreId(Long offreId) {
        this.offreId = offreId;
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

    public String getLettreMotivation() {
        return lettreMotivation;
    }

    public void setLettreMotivation(String lettreMotivation) {
        this.lettreMotivation = lettreMotivation;
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

    public String getImageProfilUrl() {
        return imageProfilUrl;
    }

    public void setImageProfilUrl(String imageProfilUrl) {
        this.imageProfilUrl = imageProfilUrl;
    }
}
