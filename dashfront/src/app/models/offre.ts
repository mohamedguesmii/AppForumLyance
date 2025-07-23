export interface Offre {
  id?: number;
  titre: string;
  description: string;
  domaine?: string;
  datePublication?: string;
  type?: string;
  duree?: string;
  evenementId?: number;
  createurId: number;
  statut?: 'acceptée' | 'refusée' | 'en attente' | string;
}
