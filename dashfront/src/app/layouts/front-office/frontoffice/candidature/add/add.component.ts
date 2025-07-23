import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CandidatureService } from 'app/services/candidature.service';
import { AuthService } from 'app/services/userService/auth.service';
import { NgForm } from '@angular/forms';

@Component({
  selector: 'app-add-candidature',
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.css']
})
export class AddComponent implements OnInit {
  offreId?: number;

  candidature = {
    telephone: '',
    email: '',
    commentaire: ''
  };

  cvFile?: File;
  lettreMotivationFile?: File;
  imageFile?: File;
  imagePreviewUrl?: string;

  cvError = '';
  lettreMotivationError = '';
  imageError = '';

  showPreview = false;

  // Notification object: message and type ('success' | 'error' | '')
  notification = { message: '', type: '' };

  isLoading = false;

  offresPostulees: number[] = []; // Offres déjà postulées par le candidat

  @ViewChild('cvInput') cvInput!: ElementRef<HTMLInputElement>;
  @ViewChild('lettreInput') lettreInput!: ElementRef<HTMLInputElement>;
  @ViewChild('imageInput') imageInput!: ElementRef<HTMLInputElement>;

  private notificationTimeout?: any;

  constructor(
    private route: ActivatedRoute,
    private candidatureService: CandidatureService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.offreId = +params['offreId'] || undefined;
    });

    this.authService.currentUser$.subscribe(user => {
      if (user && user.email) {
        this.candidature.email = user.email;
        if (user.id) {
          this.loadOffresPostulees(user.id);
        }
      }
    });
  }

  loadOffresPostulees(userId: number): void {
    this.candidatureService.getOffresPostuleesParCandidat(userId).subscribe({
      next: (ids) => {
        this.offresPostulees = ids;
      },
      error: (err) => {
        console.error('Erreur chargement des offres postulées:', err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/front/home']);
  }

  onFileChange(event: Event, type: 'cv' | 'lettreMotivation' | 'imageProfil'): void {
    this.clearErrors();

    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];

    const validTypes: Record<string, string[]> = {
      cv: [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ],
      lettreMotivation: [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ],
      imageProfil: ['image/jpeg', 'image/png']
    };

    if (!validTypes[type].includes(file.type)) {
      this.setError(type, `Type de fichier invalide pour ${type}`);
      return;
    }

    if (type === 'cv') this.cvFile = file;
    if (type === 'lettreMotivation') this.lettreMotivationFile = file;

    if (type === 'imageProfil') {
      this.imageFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreviewUrl = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  clearErrors(): void {
    this.cvError = '';
    this.lettreMotivationError = '';
    this.imageError = '';
  }

  setError(type: string, message: string): void {
    if (type === 'cv') this.cvError = message;
    if (type === 'lettreMotivation') this.lettreMotivationError = message;
    if (type === 'imageProfil') this.imageError = message;
  }

  togglePreview(form?: NgForm): void {
    if (!this.showPreview && form?.invalid) {
      form.control.markAllAsTouched();
      this.showNotification('Veuillez remplir tous les champs obligatoires avant l\'aperçu.', 'error');
      return;
    }
    this.showPreview = !this.showPreview;
    this.clearNotification();
  }

  formIsValid(form: NgForm): boolean {
    const telIsValid = /^\d{8}$/.test(this.candidature.telephone);
    return form.valid &&
           !!this.cvFile &&
           !!this.lettreMotivationFile &&
           !!this.candidature.email &&
           telIsValid;
  }

  editFile(type: 'cv' | 'lettreMotivation' | 'imageProfil'): void {
    this.showPreview = false;
    if (type === 'cv') {
      this.cvFile = undefined;
      if (this.cvInput) this.cvInput.nativeElement.value = '';
    }
    if (type === 'lettreMotivation') {
      this.lettreMotivationFile = undefined;
      if (this.lettreInput) this.lettreInput.nativeElement.value = '';
    }
    if (type === 'imageProfil') {
      this.imageFile = undefined;
      this.imagePreviewUrl = undefined;
      if (this.imageInput) this.imageInput.nativeElement.value = '';
    }
  }

  submitFromPreview(): void {
    if (!this.candidature.telephone || !this.candidature.email || !this.cvFile || !this.lettreMotivationFile) {
      this.showNotification('Formulaire incomplet, veuillez vérifier les champs obligatoires.', 'error');
      return;
    }
    this.onSubmit();
  }

  onSubmit(): void {
    const candidatId = this.authService.getUserId();
    if (!candidatId) {
      this.showNotification('Utilisateur non connecté', 'error');
      return;
    }

    // Vérification si déjà postulé à cette offre
    if (this.offreId && this.offresPostulees.includes(this.offreId)) {
      this.showNotification('Vous avez déjà postulé à cette offre.', 'error');
      return;
    }

    if (this.isLoading) return; // Empêche double soumission

    const formData = new FormData();
    formData.append('candidatId', candidatId.toString());
    formData.append('telephone', this.candidature.telephone);
    formData.append('email', this.candidature.email);
    if (this.candidature.commentaire) formData.append('commentaire', this.candidature.commentaire);
    if (this.cvFile) formData.append('cv', this.cvFile);
    if (this.lettreMotivationFile) formData.append('lettreMotivation', this.lettreMotivationFile);
    if (this.imageFile) formData.append('imageProfil', this.imageFile);
    if (this.offreId) formData.append('offreId', this.offreId.toString());

    this.isLoading = true;
    this.clearNotification();

    this.candidatureService.postuler(formData).subscribe({
      next: () => {
        this.isLoading = false;
        this.showNotification('Candidature envoyée avec succès !', 'success');
        this.resetForm();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Erreur lors de l\'envoi:', err);
        this.showNotification('Erreur lors de l\'envoi de la candidature.', 'error');
      }
    });
  }

  resetForm(): void {
    this.candidature = { telephone: '', email: this.candidature.email, commentaire: '' };
    this.cvFile = undefined;
    this.lettreMotivationFile = undefined;
    this.imageFile = undefined;
    this.imagePreviewUrl = undefined;
    this.showPreview = false;
    if (this.cvInput) this.cvInput.nativeElement.value = '';
    if (this.lettreInput) this.lettreInput.nativeElement.value = '';
    if (this.imageInput) this.imageInput.nativeElement.value = '';
  }

  // Gestion notification toast

  showNotification(message: string, type: 'success' | 'error'): void {
    this.notification = { message, type };
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }
    this.notificationTimeout = setTimeout(() => {
      this.clearNotification();
    }, 5000);
  }

  clearNotification(): void {
    this.notification = { message: '', type: '' };
  }
}
