import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Candidature } from 'app/models/candidature';

@Injectable({
  providedIn: 'root'
})
export class CandidatureService {

  private readonly apiUrl = 'http://localhost:8089/api/candidatures';

  constructor(private http: HttpClient) {}

  // 🔍 Obtenir toutes les candidatures d’un candidat
  getCandidaturesParCandidat(candidatId: number): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.apiUrl}/candidat/${candidatId}`);
  }

  // 🔍 Obtenir toutes les candidatures (admin/responsable RH)
  getAllCandidatures(): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.apiUrl}/all`);
  }

  // 📄 Obtenir la liste des offres déjà postulées par un candidat
  getOffresPostuleesParCandidat(candidatId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/offres-postulees/${candidatId}`);
  }

  // 📤 Soumettre une candidature avec fichiers
  postuler(formData: FormData): Observable<Candidature> {
    return this.http.post<Candidature>(`${this.apiUrl}/postuler`, formData);
  }

  // 🗑️ Supprimer une candidature par ID
  supprimerCandidature(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ✅ Mettre à jour le statut d'une candidature
  mettreAJourStatut(id: number, statut: string): Observable<Candidature> {
    return this.http.put<Candidature>(`${this.apiUrl}/${id}/statut?statut=${statut}`, {});
  }

  // 🖼️ Obtenir l'URL complète de l'image ou une image par défaut
  getFullImageUrl(imageProfilUrl?: string): string {
    return (!imageProfilUrl || imageProfilUrl.trim() === '')
      ? 'assets/avatar-default.png'
      : imageProfilUrl;
  }

  // ✏️ Modifier les données d'une candidature
  modifierCandidature(id: number, candidature: Partial<Candidature>): Observable<Candidature> {
    return this.http.put<Candidature>(`${this.apiUrl}/${id}`, candidature);
  }
}
