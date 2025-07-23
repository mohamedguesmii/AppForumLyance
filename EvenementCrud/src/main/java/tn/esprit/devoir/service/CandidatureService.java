package tn.esprit.devoir.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.dto.CandidatureDto;
import tn.esprit.devoir.entite.Candidature;
import tn.esprit.devoir.entite.StatutCandidature;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CandidatureService {

    // ----- Création -----



    /**
     * Enregistre une nouvelle candidature.
     * Les fichiers sont stockés en mémoire (en base sous forme de byte[]).
     */
    Candidature postuler(Candidature candidature);

    // ----- Lecture -----

    /**
     * Récupère une candidature par son ID.
     */
    Optional<Candidature> getCandidatureById(Long id);

    /**
     * Récupère toutes les candidatures associées à un candidat.
     */
    List<Candidature> getCandidaturesByUserId(Long candidatId);

    /**
     * Récupère toutes les candidatures liées à une offre.
     */
    List<Candidature> getCandidaturesParOffre(Long offreId);

    /**
     * Récupère toutes les candidatures d'un candidat sous forme DTO enrichi (email, image...).
     */
    List<CandidatureDto> getCandidaturesParCandidat(Long candidatId);

    // ----- Mise à jour -----

    /**
     * Met à jour le statut d'une candidature (ACCEPTEE, REFUSEE, etc.).
     */
    Candidature mettreAJourStatut(Long id, StatutCandidature statut, LocalDateTime dateEntretienParsed);

    // ----- Suppression -----

    /**
     * Supprime une candidature par son identifiant.
     */
    void supprimerCandidature(Long id);

    String uploadFile(MultipartFile imageProfil);


    List<Candidature> getAllCandidaturesWithCandidat();

    Candidature modifierCandidature(Long id, CandidatureDto candidatureDto);

    // ✅ Planification d'un entretien
    void planifierEntretien(Long idCandidature, LocalDateTime dateEntretien);

    boolean demarrerValidationCandidature(Long id, String s);
    /**
     * Récupère le titre d'une offre par son ID.
     */
    String getTitreOffreParId(Long offreId);

    List<CandidatureDto> getHistoriqueCandidatures();

    Candidature postulerEtAnalyser(Candidature candidature);

    List<Long> getOffresIdPostuleesParCandidat(Long candidatId);
}
