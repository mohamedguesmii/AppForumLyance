import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Evenement {
  idevent: number;
  title: string;
  description: string;
  adresse: string;
  imageUrl: string;
}

export interface ForumMatchDTO {
  evenement: Evenement;
  score: number;
}

@Injectable({
  providedIn: 'root'
})
export class MatchingService {

  private apiUrl = 'http://localhost:8089/api/matching/forums/user';

  constructor(private http: HttpClient) {}

  getMatchingForums(userId: number): Observable<ForumMatchDTO[]> {
  return this.http.get<ForumMatchDTO[]>(`http://localhost:8089/api/matching/user/${userId}`);
}

}
