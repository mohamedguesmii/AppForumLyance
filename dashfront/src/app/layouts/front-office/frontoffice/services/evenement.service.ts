import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Evenement } from 'app/models/evenement';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  uploadImage(selectedFile: any, eventId: number) {
    throw new Error('Method not implemented.');
  }

  private baseUrl = 'http://localhost:8089/api/evenements'; 

  constructor(private http: HttpClient) { }

  // Méthode pour récupérer tous les événements
  getAllEvenements(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.baseUrl}/AllEvenements`);
  }

  // Méthode pour récupérer un événement par son ID
  getEvenementById(id: number): Observable<Evenement> {
    return this.http.get<Evenement>(`${this.baseUrl}/EvenementById/${id}`);
  }

  // Méthode pour ajouter un événement
  addEvenement(evenement: Evenement): Observable<any> {
    return this.http.post(`${this.baseUrl}/add-evenement`, evenement);
  }

  // Méthode pour mettre à jour un événement
  updateEvenement(evenement: Evenement): Observable<Evenement> {
    return this.http.put<Evenement>(`${this.baseUrl}/updateEvenement`, evenement);
  }

  // Méthode pour supprimer un événement
  deleteEvenement(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/deleteEvenement/${id}`);
  }

  // Méthode pour rechercher des événements avec des critères de recherche
  searchEvenements(criteria: any): Observable<Evenement[]> {
    // Construction des paramètres de requête à partir des critères de recherche
    let params = new HttpParams();
    if (criteria.title) {
      params = params.set('title', criteria.title);
    }
    if (criteria.status) {
      params = params.set('status', criteria.status);
    }
    if (criteria.startDate) {
      params = params.set('startDate', criteria.startDate);
    }
    if (criteria.endDate) {
      params = params.set('endDate', criteria.endDate);
    }

    // Envoi de la requête avec les paramètres de requête
    return this.http.get<Evenement[]>(`${this.baseUrl}/search`, { params: params });
  }

  getAllEvents(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.baseUrl}/events/front`);
  }
  updateRating(id: number, newRating: number): Observable<Evenement> {
    const updatedEvaluation = { starRating: newRating }; // Sending only the updated property
    return this.http.put<Evenement>(`${this.baseUrl}/rating/${id}`, updatedEvaluation);
}
}
