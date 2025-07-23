import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReactionService {
  private apiUrl = 'http://localhost:8089/api/react';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token'); // ou 'access_token' selon ton app
    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }
    return headers;
  }

  getUserReaction(actualiteId: number, username: string): Observable<boolean | null> {
    return this.http.get<boolean | null>(
      `${this.apiUrl}/user-reaction/${actualiteId}/${username}`,
      { headers: this.getAuthHeaders() }
    );
  }

  addReaction(actualiteId: number, username: string, status: boolean): Observable<any> {
    const body = { actualiteId, username, status };
    return this.http.post(`${this.apiUrl}/add`, body, {
      headers: this.getAuthHeaders()
    });
  }

  getReactionCounts(actualiteId: number): Observable<{ likes: number; dislikes: number }> {
    return this.http.get<{ likes: number; dislikes: number }>(
      `${this.apiUrl}/count/${actualiteId}`,
      { headers: this.getAuthHeaders() }
    );
  }
}
