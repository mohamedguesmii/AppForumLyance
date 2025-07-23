import { User } from './user';
import { Evenement } from './evenement';

export interface Reservation {
  user: any;
  idreserv: number;

  appuser?: User;
  evenement?: Evenement;

  evenementTitle?: string;
  userEmail?: string;

  nbrPlaces: number;

  type?: string;
  description?: string;
  statut?: string;

  datereserv?: string | Date;

  nomreserv?: string;   // pour éviter les erreurs sur cette propriété
}
