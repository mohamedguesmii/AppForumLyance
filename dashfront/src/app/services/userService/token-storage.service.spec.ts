import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private TOKEN_KEY = 'auth-token';
  private USER_KEY = 'auth-user';

  public saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  public getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  signOut(): void {
    localStorage.clear();
  }

  public saveUser(user: any): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  public getUser(): any {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }
}
