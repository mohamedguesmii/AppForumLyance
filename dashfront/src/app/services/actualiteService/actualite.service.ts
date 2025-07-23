import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ActualiteService {
  addWithFormData(formData: FormData) {
    throw new Error('Method not implemented.');
  }

  private apiUrl = 'http://localhost:8089/actualites';
  api = 'http://localhost:8089/Commentaires';
  api2 = 'http://localhost:8089';

  constructor(private http: HttpClient) {}

  addData(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.apiUrl}/upload`, formData);
  }

  add(actuality, url): Observable<any> {
    const formData = {
      "actuality": actuality,
      "url": url
    };
    return this.http.post<any>(`${this.apiUrl}/add`, formData);
  }

  addWithDescription(formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/add`, formData);
  }

  getall(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/all`);
  }

 addComment(idactc: number, commentaire: string): Observable<any> {
  const body = { contenu: commentaire }; // pas besoin de username ici
  return this.http.post<any>(`${this.api}/add/${idactc}`, body);
}



  updateStatus(status: any, id: number): Observable<any> {
    return this.http.post<any>(`${this.api2}/add/${id}`, status);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/delete/${id}`);
  }

  update(id: number, actuality: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/update/${id}`, { actuality });
  }
}
