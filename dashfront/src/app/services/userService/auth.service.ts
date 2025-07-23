import { Injectable } from '@angular/core'; 
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { TokenStorageService } from './token-storage.service';
import { User } from 'app/models/user';

const AUTH_API = 'http://localhost:8089/api/auth/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

export interface RegisterUser {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  role: any;
  userRole: any;
  accessToken: string;
  token?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService
  ) {
    this.loadUserFromToken();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${AUTH_API}signin`,
      { username, password },
      httpOptions
    ).pipe(
      tap(response => {
        const token = response.accessToken || response.token;
        if (token) {
          this.tokenStorage.saveToken(token);
          this.loadUserFromToken();
          console.log('✅ Token sauvegardé:', token);
        } else {
          console.warn('⚠️ Pas de token reçu lors de la connexion');
          this.currentUserSubject.next(null);
        }
      }),
      catchError(this.handleError)
    );
  }

  register(user: RegisterUser): Observable<any> {
    return this.http.post(
      `${AUTH_API}signup`,
      user,
      httpOptions
    ).pipe(
      catchError(this.handleError)
    );
  }

  logout(): void {
    this.tokenStorage.signOut();
    this.currentUserSubject.next(null);
  }

  /**
   * Charge et décode le token JWT stocké, puis met à jour currentUserSubject.
   */
  private loadUserFromToken(): void {
    const token = this.tokenStorage.getToken();
    if (!token) {
      this.currentUserSubject.next(null);
      return;
    }
    const user = this.decodeUserFromToken(token);
    this.currentUserSubject.next(user);
  }

  /**
   * Décode un token JWT et extrait un objet User.
   */
  private decodeUserFromToken(token: string): User | null {
    try {
      const payloadBase64 = token.split('.')[1];
      const payloadJson = atob(payloadBase64);
      const payload = JSON.parse(payloadJson);

      const user: User = {
        id: payload.id || undefined,
        username: payload.sub || payload.username || '',
        email: payload.email || '',
        firstName: payload.firstName || '',
        lastName: payload.lastName || '',
        phoneNumber: payload.phoneNumber || '',
        roles: (payload.roles || []).map((r: any) => typeof r === 'string' ? { name: r } : r),
        role: '',
        avatarUrl: '',
        name: ''
      };

      return user;
    } catch (e) {
      console.error('Erreur décodage token JWT', e);
      return null;
    }
  }

  /**
   * Récupère l'ID de l'utilisateur connecté.
   */
  getUserId(): number | null {
    const user = this.currentUserSubject.value;
    if (user && user.id) {
      return user.id;
    }
    return null;
  }

  /**
   * Renvoie le rôle principal sous forme de string.
   */
  getUserRole(): string {
    const user = this.currentUserSubject.value;
    if (!user || !user.roles || user.roles.length === 0) return 'Rôle inconnu';

    const roleName = user.roles[0]?.name;
    console.log("📌 Rôle extrait dans getUserRole():", roleName);

    return roleName || 'Rôle inconnu';
  }

  /**
   * Renvoie le nom complet de l'utilisateur connecté.
   */
  getUserFullName(): string {
    const user = this.currentUserSubject.value;
    if (!user) return '';
    return `${user.firstName} ${user.lastName}`.trim();
  }

  resetPassword(token: string, password: string): Observable<any> {
    if (!token || !password) {
      throw new Error('Token et mot de passe sont requis');
    }
    const body = { token, password };
    return this.http.post(`${AUTH_API}reset-password`, body, httpOptions);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${AUTH_API}forgot-password`, { email }, httpOptions).pipe(
      tap(() => {
        console.log(`📨 Demande de réinitialisation envoyée pour : ${email}`);
      }),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    if (error.error instanceof ErrorEvent) {
      console.error('Erreur client:', error.error.message);
    } else {
      console.error(`Erreur serveur ${error.status}: ${JSON.stringify(error.error)}`);
    }
    return throwError(() => new Error('Erreur de communication avec le serveur.'));
  }
}

export { User };
