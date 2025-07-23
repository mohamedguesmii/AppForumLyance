import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Evenement } from './evenement.service'; // Réutilise le modèle Evenement

@Injectable({
  providedIn: 'root',
})
export class EvenementAvenirService {
  private baseUrl = 'http://localhost:8089/api/evenements/scraping';

  constructor(private http: HttpClient) {}

  // Récupérer la liste des événements scrappés à venir
  getEvenementsScrappesAvenir(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.baseUrl}/avenir`);
  }

  // Ajouter un événement scrappé
  addEvenementScrapingAuto(evenement: Evenement): Observable<Evenement> {
    return this.http.post<Evenement>(`${this.baseUrl}/auto`, evenement);
  }

  // Lancer le scraping (POST /scrape-now)
  lancerScraping(): Observable<string> {
  return this.http.post(this.baseUrl + '/scrape-now', {}, { responseType: 'text' });
}


}
