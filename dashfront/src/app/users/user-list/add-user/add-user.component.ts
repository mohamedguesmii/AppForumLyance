import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from 'app/services/userService/user.service';
import { User } from 'app/models/user';
import { finalize } from 'rxjs/operators';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
})
export class AddUserComponent {
  addUserForm: FormGroup;
  isLoading = false;
  showForm: boolean = false; // au cas où tu veux toggle un formulaire

  constructor(
    private fb: FormBuilder,
    private userService: UserService
  ) {
    this.addUserForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/), Validators.maxLength(30)]],
      lastName: ['', [Validators.required, Validators.pattern(/^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/), Validators.maxLength(30)]],
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(20), Validators.pattern(/^[a-zA-Z0-9._-]+$/)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{8,15}$/)]],
      birthDate: [''],
      address: ['', [Validators.maxLength(100)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit() {
    if (this.addUserForm.invalid) {
      this.addUserForm.markAllAsTouched();
      this.isLoading = false;
      return;
    }

    this.isLoading = true;
    const formData: User = this.addUserForm.value;

    this.userService.create(formData).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: 'Utilisateur ajouté avec succès !',
          timer: 3000,
          showConfirmButton: false,
          position: 'top'
        });
        this.addUserForm.reset();
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de l’ajout : ' + (err?.error?.message || 'erreur inconnue'),
          confirmButtonText: 'Fermer',
          position: 'top'
        });
      }
    });
  }
}
