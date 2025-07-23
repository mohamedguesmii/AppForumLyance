export interface CandidatureSummaryDTO {
  idUser: number;
  username: string;
  user: any;
  id: number;
  nom: string;
  prenom: string;
  email: string;
  imageUrl: string;
}

export interface RendezVousDTO {
  email: any;
  lienVisio: any;
  token: string;
  candidatId: number;
  date: string | number | Date;
  id: number;
  dateHeure: string;
  tokenAcces: string;
  actif: boolean;
  statut: string;
  candidature?: CandidatureSummaryDTO | null;
}
