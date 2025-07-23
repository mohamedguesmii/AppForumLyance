import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from 'app/services/userService/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit, OnDestroy {
  forgotPasswordForm!: FormGroup;
  message: string = '';
  errorMessage: string = '';
  originalTitle = document.title;
  private titleInterval: any;
showFinalNotification: any;
showInitialNotification: any;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnDestroy(): void {
    this.clearTitleAnimation();
  }

  get email() {
    return this.forgotPasswordForm.get('email')!;
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

    const email = this.forgotPasswordForm.value.email;

    this.authService.forgotPassword(email).subscribe({
      next: (res: any) => {
        this.errorMessage = '';
        this.forgotPasswordForm.reset();

        // Première notification info
        this.message = "Si cet email existe dans notre système, les instructions de réinitialisation ont été envoyées.";
        const snackRef = this.snackBar.open(this.message, 'OK', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['custom-snackbar-info'] // classe CSS à créer
        });

        this.animateTitle('🔔 ' + this.message, 3000);
        this.playNotificationSound();

        // Après 3 secondes, fermer la notification info et afficher la notification succès
        snackRef.afterDismissed().subscribe(() => {
          this.message = "L'email a bien été envoyé !";
          this.snackBar.open(this.message, 'OK', {
            duration: 4000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['custom-snackbar-success']
          });
          this.animateTitle('✅ ' + this.message, 4000);
          this.playNotificationSound();
        });
      },
      error: (err) => {
        this.message = '';
        this.errorMessage = "Erreur lors de la demande, veuillez réessayer.";
        this.snackBar.open(this.errorMessage, 'OK', {
          duration: 7000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['custom-snackbar-error']
        });
        this.animateTitle('⚠️ ' + this.errorMessage, 7000);
        this.playNotificationSound();
      }
    });
  }

  animateTitle(message: string, duration: number) {
    this.clearTitleAnimation();
    let showMessage = true;
    this.titleInterval = setInterval(() => {
      document.title = showMessage ? message : this.originalTitle;
      showMessage = !showMessage;
    }, 500);

    setTimeout(() => {
      this.clearTitleAnimation();
      document.title = this.originalTitle;
    }, duration);
  }

  clearTitleAnimation() {
    if (this.titleInterval) {
      clearInterval(this.titleInterval);
      this.titleInterval = null;
    }
  }

  playNotificationSound() {
    try {
      const audio = new Audio('/assets/notification.mp3');
      audio.play();
    } catch (e) {
      console.warn('Le son de notification n’a pas pu être joué.', e);
    }
  }
}
