import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/userService/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerData = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    address: '',
    phoneNumber: '',
    role: ''
  };

  passwordStrength = '';
  passwordStrengthClass = '';
  usernameStatus = '';
  usernameAvailable = true;
  passwordStrengthPercent = 0;

  showPassword = false;
  showConfirmPassword = false;
  passwordsMatch: any;
  isLoading: any;

  constructor(private authService: AuthService, private router: Router) {}

  onRegister(): void {
    if (this.registerData.password !== this.registerData.confirmPassword) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: 'Les mots de passe ne correspondent pas.'
      });
      return;
    }

    const userToRegister = {
      firstName: this.registerData.firstName,
      lastName: this.registerData.lastName,
      username: this.registerData.username,
      email: this.registerData.email,
      password: this.registerData.password,
      address: this.registerData.address,
      phoneNumber: this.registerData.phoneNumber,
      role: this.registerData.role
    };

    this.authService.register(userToRegister).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Inscription réussie !',
          text: 'Vous pouvez maintenant vous connecter.',
          timer: 2000,
          showConfirmButton: false,
          timerProgressBar: true
        }).then(() => {
          this.router.navigate(['/login']);
        });
      },
      error: (err) => {
        console.error('Erreur inscription:', err);
        const message = err.error?.message ?? 'Erreur lors de l’inscription. Veuillez vérifier vos informations.';
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: message
        });
      }
    });
  }

  checkPasswordStrength() {
    const pwd = this.registerData.password || '';
    let strength = 0;

    if (pwd.length >= 8) strength += 1;
    if (/[A-Z]/.test(pwd)) strength += 1;
    if (/[a-z]/.test(pwd)) strength += 1;
    if (/[0-9]/.test(pwd)) strength += 1;
    if (/[^A-Za-z0-9]/.test(pwd)) strength += 1;

    switch (strength) {
      case 0:
      case 1:
      case 2:
        this.passwordStrength = 'Faible';
        this.passwordStrengthClass = 'bg-danger';
        this.passwordStrengthPercent = 30;
        break;
      case 3:
      case 4:
        this.passwordStrength = 'Moyenne';
        this.passwordStrengthClass = 'bg-warning';
        this.passwordStrengthPercent = 60;
        break;
      case 5:
        this.passwordStrength = 'Forte';
        this.passwordStrengthClass = 'bg-success';
        this.passwordStrengthPercent = 100;
        break;
    }
  }

  checkPasswordsMatch(): void {
    if (this.registerData.confirmPassword) {
      this.passwordsMatch = this.registerData.password === this.registerData.confirmPassword;
    } else {
      this.passwordsMatch = true;
    }
  }

  autoSuggestName(): void {
    const email = this.registerData.email;
    if (!this.registerData.firstName && email) {
      const parts = email.split('@')[0].split(/[._\-]/);
      this.registerData.firstName = this.capitalize(parts[0]);
      this.registerData.lastName = parts.length > 1 ? this.capitalize(parts[1]) : '';
    }
  }

  checkUsernameAvailability(): void {
    const username = this.registerData.username.toLowerCase();
    const usedUsernames = ['admin', 'test', 'demo'];
    if (usedUsernames.includes(username)) {
      this.usernameAvailable = false;
      this.usernameStatus = 'Nom d’utilisateur déjà utilisé.';
    } else {
      this.usernameAvailable = true;
      this.usernameStatus = 'Nom d’utilisateur disponible.';
    }
  }

  capitalize(word: string): string {
    return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
}
