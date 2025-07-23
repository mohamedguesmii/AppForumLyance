import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

interface ChatRequest {
  userId: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = 'http://localhost:8089/api/chatbot/message'; // adapte si besoin

  constructor(private http: HttpClient) {}

  sendMessage(userId: string, message: string): Observable<any> {
    const body: ChatRequest = { userId, message };
    return this.http.post<any>(this.apiUrl, body);
  }
}
