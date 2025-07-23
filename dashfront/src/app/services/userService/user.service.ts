import { TokenStorageService } from './token-storage.service';
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { User } from 'app/models/user';
import { UserUpdatePayload } from 'app/models/UserUpdatePayload';
import { Role } from 'app/models/role';

const API_URL = 'http://localhost:8089/api/test/';

@Injectable({
  providedIn: 'root'
})
export class UserService {
   // updateRoles(id: number, rolesNames: string[]): any {
  ///      throw new Error('Method not implemented.');
//}
  private apiUrl = 'http://localhost:8089/api/user';

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) {}

  private getAuthHeaders(): HttpHeaders {
  const token = this.tokenStorage.getToken();
  console.log('Token dans getAuthHeaders:', token);
  if (!token) {
    console.warn('Aucun token trouvé dans sessionStorage');
    return new HttpHeaders();
  }
  return new HttpHeaders({
    'Authorization': `Bearer ${token}`
  });
}


  private handleError(error: any) {
  console.error('Une erreur est survenue :', error);

  // Affiche les détails s’ils existent
  if (error.error) {
    console.error('Détails de l\'erreur:', error.error);
  }

  let message = 'Erreur inconnue';

  if (error.message) message = error.message;
  else if (typeof error === 'string') message = error;

  return throwError(() => new Error(message));
}


   
  getAll(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/all`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  getUserBoard(): Observable<any> {
    return this.http.get(API_URL + 'user', { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  getAdminBoard(): Observable<any> {
    return this.http.get(API_URL + 'admin', { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  create(user: User): Observable<User> {
    return this.http.post<User>(this.apiUrl, user, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

 update(id: number, user: UserUpdatePayload): Observable<User> {
    console.log("Requête envoyée au backend :", user);
  const headers = this.getAuthHeaders();
  return this.http.put<User>(`${this.apiUrl}/${id}`, user, { headers }).pipe(
    catchError(this.handleError)
  );
}




  delete(id: number): Observable<any> {
  const headers = this.getAuthHeaders();
  console.log('Headers dans delete:', headers);
  return this.http.delete(`${this.apiUrl}/${id}`, { headers, responseType: 'text' }).pipe(
    catchError(this.handleError)
  );
}


 updateRoles(userId: number, roles: string[]): Observable<User> {
    const url = `${this.apiUrl}/${userId}/roles`;
    return this.http.put<User>(url, roles, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }


  
getUserRoles(userId: number): Observable<Role[]> {
  const headers = this.getAuthHeaders();
  const url = `${this.apiUrl}/${userId}/roles`;
  return this.http.get<Role[]>(url, { headers }).pipe(
    catchError(this.handleError)
  );
}





}
