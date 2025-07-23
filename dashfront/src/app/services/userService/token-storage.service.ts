import { Injectable } from '@angular/core';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  constructor() {}

  // Supprime les données stockées
  signOut(): void {
    window.localStorage.clear();
  }

  // Sauvegarde le token
  public saveToken(token: string): void {
    console.log('✅ Token sauvegardé :', token);
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.setItem(TOKEN_KEY, token);
  }

  // Récupère le token
  public getToken(): string | null {
    const token = window.localStorage.getItem(TOKEN_KEY);
    console.log('📦 Token récupéré :', token);
    return token;
  }

  // Sauvegarde l'utilisateur
  public saveUser(user: any): void {
    console.log('✅ Utilisateur sauvegardé :', user);
    window.localStorage.removeItem(USER_KEY);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  // Récupère l'utilisateur
  public getUser(): any | null {
    const user = window.localStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return null;
  }
}
