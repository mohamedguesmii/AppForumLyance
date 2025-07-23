import { Injectable } from '@angular/core';
import { Reservation } from '../models/reservation';
import { Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:8089/api/reservation/';
  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  }
  constructor(private http:HttpClient ) { }

  addReservationAndAssignEvent(reservation: Reservation, idEvent: number): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.apiUrl}assign/${idEvent}`, reservation);
  }
}
