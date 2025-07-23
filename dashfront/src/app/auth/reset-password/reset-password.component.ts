import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'app/services/userService/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm!: FormGroup;
  token!: string | null;
  errorMessage: string | null = null;
  successMessage: string | null = null;
showConfirmPassword: any;
showPassword: any;


  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Récupère le token dans les paramètres URL : /reset-password?token=abc123
    this.token = this.route.snapshot.queryParamMap.get('token');

    if (!this.token) {
      this.errorMessage = 'Le token est manquant ou invalide.';
    }

    this.resetPasswordForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  // Validateur personnalisé pour vérifier si password et confirmPassword correspondent
  passwordMatchValidator: ValidatorFn = (group: FormGroup): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return password === confirm ? null : { notMatching: true };
  };

  onSubmit(): void {
    if (!this.token) {
      this.errorMessage = 'Token manquant, impossible de réinitialiser le mot de passe.';
      return;
    }

    if (this.resetPasswordForm.valid) {
      const newPassword = this.resetPasswordForm.value.password;

      this.authService.resetPassword(this.token, newPassword).subscribe({
        next: () => {
          this.successMessage = 'Mot de passe réinitialisé avec succès ! Redirection vers la page de connexion...';
          this.errorMessage = null;
          setTimeout(() => this.router.navigate(['/login']), 3000);
        },
        error: (err) => {
          this.errorMessage = 'Token invalide ou expiré.';
          this.successMessage = null;
          console.error('Erreur reset password:', err);
        }
      });
    } else {
      this.errorMessage = 'Veuillez remplir correctement le formulaire.';
      this.successMessage = null;
    }
  }
}
