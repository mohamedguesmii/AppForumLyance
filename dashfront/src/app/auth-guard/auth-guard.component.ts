import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { TokenStorageService } from 'app/services/userService/token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private tokenStorageService: TokenStorageService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (!!this.tokenStorageService.getToken()) {
      // Token présent => accès autorisé
      return true;
    }
    // Pas de token => redirection vers login
    this.router.navigate(['/login']);
    return false;
  }
}
