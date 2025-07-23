import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Evenement } from 'app/models/evenement';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  getEvenements: any;
  getEvenementsAvenir: any;
  getAll() {
    throw new Error('Method not implemented.');
  }
  private baseUrl = 'http://localhost:8089/api/evenements';

  constructor(private http: HttpClient) {}

  getAllEvenements(): Observable<Evenement[]> {
    return this.http.get<Evenement[]>(`${this.baseUrl}/all`);
  }

  addEvenement(formData: FormData): Observable<HttpEvent<any>> {
    return this.http.post<any>(`${this.baseUrl}`, formData, {
      reportProgress: true,
      observe: 'events'
    });
  }

 updateEvenement(idevent: number, formData: FormData): Observable<HttpEvent<any>> {
  return this.http.put<any>(`${this.baseUrl}/${idevent}`, formData, {
    reportProgress: true,
    observe: 'events'
  });
}


  deleteEvenement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  updateRating(id: number, newRating: number): Observable<Evenement> {
    const updatedEvaluation = { starRating: newRating };
    return this.http.put<Evenement>(`${this.baseUrl}/rating/${id}`, updatedEvaluation);
  }

  uploadEventImage(idevent: number, file: File): Observable<Evenement> {
    const formData = new FormData();
    formData.append('fileImage', file);
    return this.http.post<Evenement>(`${this.baseUrl}/upload-image/${idevent}`, formData);
  }

  searchEvenements(criteria: { title?: string; status?: string; startDate?: string; endDate?: string }): Observable<Evenement[]> {
    const formData = new FormData();

    if (criteria.title) formData.append('title', criteria.title);
    if (criteria.status) formData.append('status', criteria.status);
    if (criteria.startDate) formData.append('startDate', criteria.startDate);
    if (criteria.endDate) formData.append('endDate', criteria.endDate);

    return this.http.post<Evenement[]>(`${this.baseUrl}/search`, formData);
  }


  getEvenementById(id: number): Observable<Evenement> {
  return this.http.get<Evenement>(`${this.baseUrl}/${id}`);
}

getEvenementsManuels(): Observable<Evenement[]> {
  return this.http.get<Evenement[]>('http://localhost:8089/api/evenements/avenir-manuel');
}

getEvenementsScrapes(): Observable<Evenement[]> {
  return this.http.get<Evenement[]>('http://localhost:8089/api/evenements/scraping/avenir');
}





}

export { Evenement };
