// statut-candidature.enum.ts
export enum StatutCandidature {
  EN_ATTENTE = 'EN_ATTENTE',
  ACCEPTEE = 'ACCEPTEE',
  REFUSEE = 'REFUSEE',
  ENTRETIEN_PLANIFIE = 'ENTRETIEN_PLANIFIE',
  ARCHIVEE = 'ARCHIVEE'
}

// candidature.model.ts

export interface Candidature {
  offre: any;
  imageUrl: any;
  candidat: any;
  id?: number;                     // Identifiant unique de la candidature (optionnel à la création)
  candidatId: number;              // ID du candidat
  offreId: number;                 // ID de l’offre liée

  dateCandidature?: string;        // Date de candidature en format ISO (ex: "2025-06-19T15:00:00Z")
  statut?: StatutCandidature;      // Statut actuel de la candidature

  cvUrl?: string;                  // URL vers le CV (hébergé sur serveur ou cloud)
  lettreMotivation?: string;       // Texte ou URL lettre de motivation
  commentairesRecruteur?: string;  // Commentaires éventuels du recruteur

  telephone?: string;              // Téléphone du candidat
  email?: string;                  // Email du candidat

  dateMiseAJour?: string;          // Date dernière mise à jour (ISO string)
  dateEntretien?: string;          // Date de l’entretien (ISO string)

  imageProfilUrl?: string;         // URL vers image de profil (si disponible)


  // Champs ajoutés :
  titreOffre?: string;
  typeOffre?: string;
}
