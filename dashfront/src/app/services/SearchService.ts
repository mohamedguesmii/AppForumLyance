import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

// Interface représentant un document retourné par le backend
export interface Document {
  id: number;
  type: string;
  content: string;
  enrichedContent?: string;  // conversion du champ enriched_content
  distance?: number;
}

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  private apiUrl = 'http://localhost:8089/api/search'; // URL de ton API backend

  constructor(private http: HttpClient) {}

  // Méthode de recherche qui envoie la requête au backend et transforme la réponse
  search(query: string): Observable<Document[]> {
    return this.http.post<any[]>(this.apiUrl, { query }).pipe(
      map(results => results.map(doc => ({
        id: doc.id,
        type: doc.type,
        content: doc.content,
        enrichedContent: doc.enriched_content, // conversion snake_case -> camelCase
        distance: doc.distance
      }))),
      catchError(this.handleError)
    );
  }

  // Gestion centralisée des erreurs HTTP
  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    // Tu peux personnaliser le message ou la logique ici
    return throwError(() => new Error('Une erreur est survenue lors de la recherche.'));
  }
}
