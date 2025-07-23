import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { TokenStorageService } from 'app/services/userService/token-storage.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // Liste d'URLs où on ne met pas le token (ex: login, inscription, email sans auth)
  private excludedUrls: string[] = [
    '/api/auth/login',
    '/api/auth/register',
    //'/api/email/send' // Exclure si ce endpoint ne nécessite pas d'auth
  ];

  constructor(private router: Router, private tokenStorage: TokenStorageService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Ne pas modifier la requête pour les URLs exclues
    if (this.excludedUrls.some(url => req.url.includes(url))) {
      return next.handle(req);
    }

    const token = this.tokenStorage.getToken();

    // Cloner la requête pour ajouter le header Authorization si token présent
    let authReq = req;
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(authReq).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401 || err.status === 403) {
          // Déconnexion et redirection vers login si non autorisé
          this.tokenStorage.signOut();
          this.router.navigate(['/login']);
        }
        return throwError(() => err);
      })
    );
  }
}
