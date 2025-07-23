import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface Offre avec champs optionnels bien typés
export interface Offre {
  createurId: number;
  id?: number;  // optionnel à la création
  titre: string;
  description: string;
  domaine: string;
  datePublication: string;
  statut: string;
  type?: string;
  duree?: string;
  evenementId: number;  // obligatoire, pas optionnel ni nullable
}

@Injectable({
  providedIn: 'root'
})
export class OffreService {

  private apiUrl = 'http://localhost:8089/api/offres'; // adapter selon ton backend

  constructor(private http: HttpClient) {}

  // Méthode privée pour nettoyer un objet en supprimant les propriétés à undefined
  private cleanObject<T>(obj: T): T {
    const cleaned = { ...obj };
    Object.keys(cleaned).forEach(key => {
      if (cleaned[key as keyof T] === undefined) {
        delete cleaned[key as keyof T];
      }
    });
    return cleaned;
  }

  // Ajouter une nouvelle offre (POST)
  ajouterOffre(offre: Offre): Observable<Offre> {
    const offreClean = this.cleanObject(offre);
    return this.http.post<Offre>(this.apiUrl, offreClean);
  }

  // Obtenir toutes les offres (GET)
  getAllOffres(): Observable<Offre[]> {
    return this.http.get<Offre[]>(this.apiUrl);
  }

  // Obtenir une offre par son ID (GET)
  getOffreById(id: number): Observable<Offre> {
    return this.http.get<Offre>(`${this.apiUrl}/${id}`);
  }

  // Mettre à jour une offre (PUT)
  updateOffre(id: number, offre: Offre): Observable<Offre> {
    const offreClean = this.cleanObject(offre);
    return this.http.put<Offre>(`${this.apiUrl}/${id}`, offreClean);
  }

  // Supprimer une offre (DELETE)
  deleteOffre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
