import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/userService/auth.service';
import { TokenStorageService } from '../../services/userService/token-storage.service';

interface LoginData {
  username: string; // Remplacé email par username
  password: string;
  rememberMe: boolean;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
signInWithLinkedIn() {
throw new Error('Method not implemented.');
}
  loginData: LoginData = {
    username: '',
    password: '',
    rememberMe: false
  };

  loginError: string | null = null;
  showPassword: boolean = false;

  constructor(
    private router: Router,
    private authService: AuthService,
    private tokenStorage: TokenStorageService
  ) {}

  ngOnInit(): void {
    const remembered = localStorage.getItem('rememberMe') === 'true';
    const savedUsername = localStorage.getItem('username');

    if (remembered && savedUsername) {
      this.loginData.username = savedUsername;
      this.loginData.rememberMe = true;
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

onLogin(): void {
  this.loginError = null;

  const { username, password, rememberMe } = this.loginData;

  if (!username || !password) {
    this.loginError = 'Veuillez remplir tous les champs 🤖';
    return;
  }

  if (username.length < 3 || /\s/.test(username)) {
    this.loginError = "Le nom utilisateur doit contenir au moins 3 caractères sans espaces.";
    return;
  }

  if (password.length < 6) {
    this.loginError = 'Votre mot de passe semble un peu court 🤔';
    return;
  }

  this.authService.login(username, password).subscribe({
    next: (response) => {
      console.log('Réponse complète login:', response);

      const token = response.token || response.accessToken || '';
      const role = response.role || response.userRole || 'Candidat'; // ✅ récupère le rôle

      if (token) {
        this.tokenStorage.saveToken(token);

        // ✅ Sauvegarde nom et rôle dans le localStorage
        localStorage.setItem('username', username);
        localStorage.setItem('role', role); // ✅ ligne ajoutée ici !

        if (rememberMe) {
          localStorage.setItem('rememberMe', 'true');
        } else {
          localStorage.removeItem('rememberMe');
        }

        this.router.navigate(['/dashboard']);
      } else {
        console.error('Token absent dans la réponse login:', response);
        this.loginError = "Problème lors de la récupération du token.";
      }
    },
    error: () => {
      this.loginError = "Nom utilisateur ou mot de passe incorrect. Essayez encore ! 🤖";
    }
  });
}


  signInWithGithub(): void {
    window.open('https://github.com/login/oauth/authorize?...', '_self');
  }

  signInWithFacebook(): void {
    window.open('https://www.facebook.com/v10.0/dialog/oauth?...', '_self');
  }

  signInWithGoogle(): void {
    window.open('https://accounts.google.com/o/oauth2/v2/auth?...', '_self');
  }

  goBack(): void {
  this.router.navigate(['/front']);
}

buttonText: string = '← Retour à l\'accueil';

onHover(isHovering: boolean): void {
  this.buttonText = isHovering ? '🚀 Retour rapide !' : '← Retour à l\'accueil';
}

onClick(): void {
  this.buttonText = 'À tout de suite ! 👋';
}

onRelease(): void {
  this.buttonText = '← Retour à l\'accueil';
}


}
