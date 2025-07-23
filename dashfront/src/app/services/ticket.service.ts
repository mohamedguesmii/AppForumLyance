import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from 'app/models/ticket';

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  private baseUrl = 'http://localhost:8089/api/tickets';

  constructor(private http: HttpClient) { }
  
  getAllTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/allTickets`);
  }

  getTicketById(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.baseUrl}/ticketById/${id}`);
  }

  addTicket(ticket: Ticket): Observable<any> {
    return this.http.post(`${this.baseUrl}/add-ticket`, ticket);
  }

  updateTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.baseUrl}/updateTicket`, ticket);
  }

  deleteTicket(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/deleteTicket/${id}`);
  }
}
