import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Reservation } from 'app/models/reservation';
import { Evenement } from 'app/models/evenement';

// Interface pour les stats participants (à adapter si besoin)
export interface ParticipantStat {
  evenement: string;
  role: string;
  count: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private baseUrl = 'http://localhost:8089/api/reservations';
  private statsUrl = 'http://localhost:8089/api/stats';

  constructor(private http: HttpClient) { }

  // CRUD réservations

  getAllReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.baseUrl}/all`).pipe(
      map(reservations =>
        reservations.map(res => {
          if (res.evenement) {
            res.evenement = this.mapEvenement(res.evenement);
          }
          return res;
        })
      )
    );
  }

  getReservationById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.baseUrl}/${id}`).pipe(
      map(res => {
        if (res.evenement) {
          res.evenement = this.mapEvenement(res.evenement);
        }
        return res;
      })
    );
  }

  addReservation(reservation: Reservation): Observable<any> {
    return this.http.post(`${this.baseUrl}/add`, reservation);
  }

  updateReservation(reservation: Reservation): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.baseUrl}/update`, reservation);
  }

  deleteReservation(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`);
  }

  reserverEvenement(idEvent: number, idUser: number): Observable<Reservation> {
    return this.http.post<Reservation>(
      `${this.baseUrl}/reserver`,
      { evenementId: idEvent, userId: idUser }
    ).pipe(
      map(res => {
        if (res.evenement) {
          res.evenement = this.mapEvenement(res.evenement);
        }
        return res;
      })
    );
  }

  // Nouvelle méthode pour récupérer les stats participants
  getParticipantStats(): Observable<ParticipantStat[]> {
    return this.http.get<ParticipantStat[]>(`${this.statsUrl}/participants`);
  }

  // Méthode privée pour mapper un objet Evenement reçu du backend
  private mapEvenement(rawEvent: any): Evenement {
    return {
      ...rawEvent,
      titre: rawEvent.titre || rawEvent.title || 'Sans titre',
      body: rawEvent.body,
      name: rawEvent.name,
      comments: rawEvent.comments,
      likes: rawEvent.likes,
      idevent: rawEvent.idevent,
      title: rawEvent.title,
      description: rawEvent.description,
      capacity: rawEvent.capacity,
      status: rawEvent.status,
      datedebut: rawEvent.datedebut,
      datefin: rawEvent.datefin,
      adresse: rawEvent.adresse,
      imageUrl: rawEvent.imageUrl,
      starRating: rawEvent.starRating,
      imagecloud: rawEvent.imagecloud
    };
  }
}
