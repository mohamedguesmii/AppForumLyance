import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RendezVousDTO } from 'app/models/RendezVousDTO';
import { EmailEntretienRequest } from 'app/models/email-entretien-request';

@Injectable({
  providedIn: 'root'
})
export class RendezVousService {

  private apiUrl = 'http://localhost:8089/api/rendezvous';

  constructor(private http: HttpClient) { }

  getRendezVousPlanifiesAvecCandidature(): Observable<RendezVousDTO[]> {
    return this.http.get<RendezVousDTO[]>(`${this.apiUrl}/entretien-planifie`);
  }

  creerRendezVous(rdv: RendezVousDTO): Observable<RendezVousDTO> {
    return this.http.post<RendezVousDTO>(this.apiUrl, rdv);
  }

  associerCandidature(rdvId: number, candidatureId: number): Observable<RendezVousDTO> {
    return this.http.put<RendezVousDTO>(`${this.apiUrl}/${rdvId}/candidature/${candidatureId}`, {});
  }

  getRendezVousDTOByToken(token: string): Observable<RendezVousDTO> {
    return this.http.get<RendezVousDTO>(`${this.apiUrl}/${token}`);
  }

  supprimerRendezVous(id: number): Observable<string> {
  return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
}

  // Nouvelle méthode pour modifier le statut du rendez-vous
  modifierStatutRendezVous(id: number, nouveauStatut: string): Observable<void> {
  return this.http.put<void>(`${this.apiUrl}/${id}/statut`, { statut: nouveauStatut }, {
    headers: { 'Content-Type': 'application/json' }
  });
}

updateRendezVous(id: number, rdv: RendezVousDTO): Observable<RendezVousDTO> {
  return this.http.put<RendezVousDTO>(`${this.apiUrl}/${id}/update`, rdv);
}



envoyerEmailEntretien(payload: { email: string, dateHeure: string, lienVisio: string }): Observable<any> {
  console.log("🔵 [Service] Payload envoyé:", payload);
  return this.http.post<any>(`${this.apiUrl}/send-email-entretien`, payload);
}








}
