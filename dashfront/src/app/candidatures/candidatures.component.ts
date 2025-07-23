import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CandidatureService } from 'app/services/candidature.service';
import { AuthService } from 'app/services/userService/auth.service';
import { Candidature, StatutCandidature } from 'app/models/candidature';
import { EmailService, EmailRequest } from 'app/services/EmailService';
import { CandidatureUpdateDto } from 'app/models/CandidatureUpdateDto';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-candidatures',
  templateUrl: './candidatures.component.html',
  styleUrls: ['./candidatures.component.css'],
})
export class CandidaturesComponent implements OnInit {
  candidatures: (Candidature & { fullImageUrl: string; titreOffre?: string; typeOffre?: string })[] = [];
  filteredCandidatures: (Candidature & { fullImageUrl: string; titreOffre?: string; typeOffre?: string })[] = [];

  loading = false;
  errorMsg = '';

  page = 1;
  pageSize = 10;
  totalPages = 1;

  searchTerm = '';
  statutFiltre: string = 'TOUS';
  statuts: string[] = [];

  selectedCandidature: Candidature & { fullImageUrl?: string } = {} as any;
  modalModifierVisible = false;

  constructor(
    private candidatureService: CandidatureService,
    private authService: AuthService,
    private emailService: EmailService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.statuts = Object.values(StatutCandidature);
    const role = this.authService.getUserRole();

    if (role === 'ROLE_ADMINISTRATEUR') {
      this.loadAllCandidatures();
    } else {
      const candidatId = this.authService.getUserId();
      if (candidatId != null) {
        this.loadCandidatures(candidatId);
      } else {
        this.errorMsg = 'Utilisateur non connecté.';
        this.candidatures = [];
        this.filteredCandidatures = [];
      }
    }
  }

  loadCandidatures(candidatId: number): void {
    this.loading = true;
    this.errorMsg = '';
    this.candidatureService.getCandidaturesParCandidat(candidatId).subscribe({
      next: (data) => {
        this.candidatures = data.map((c) => ({
          ...c,
          fullImageUrl: this.getFullImageUrl(
            c.imageProfilUrl ?? c.imageUrl ?? c.candidat?.imageUrl
          ),
          email: c.email ?? c.candidat?.email ?? 'N/A',
          titreOffre: c.offre?.titre ?? '',
          typeOffre: c.offre?.type ?? '',
        }));
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = 'Erreur lors du chargement des candidatures.';
        console.error(err);
        this.loading = false;
      },
    });
  }

  loadAllCandidatures(): void {
    this.loading = true;
    this.errorMsg = '';
    this.candidatureService.getAllCandidatures().subscribe({
      next: (data) => {
        this.candidatures = data.map((c) => ({
          ...c,
          fullImageUrl: this.getFullImageUrl(c.imageUrl ?? c.candidat?.imageUrl),
          email: c.email ?? c.candidat?.email ?? 'N/A',
          titreOffre: c.offre?.titre ?? '',
          typeOffre: c.offre?.type ?? '',
        }));
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = 'Erreur lors du chargement des candidatures.';
        console.error(err);
        this.loading = false;
      },
    });
  }

  applyFilter(resetPage = true): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredCandidatures = this.candidatures.filter((c) => {
      const matchesSearch =
        (c.telephone?.toLowerCase().includes(term) ?? false) ||
        (c.email?.toLowerCase().includes(term) ?? false) ||
        (c.commentairesRecruteur?.toLowerCase().includes(term) ?? false);

      const matchesStatut =
        this.statutFiltre === 'TOUS' || c.statut === this.statutFiltre;

      return matchesSearch && matchesStatut;
    });

    this.totalPages = Math.max(
      1,
      Math.ceil(this.filteredCandidatures.length / this.pageSize)
    );

    if (resetPage) {
      this.page = 1;
    } else if (this.page > this.totalPages) {
      this.page = this.totalPages;
    }
  }

  get pagedCandidatures(): (Candidature & { fullImageUrl: string; titreOffre?: string; typeOffre?: string })[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filteredCandidatures.slice(start, start + this.pageSize);
  }

  changePage(newPage: number): void {
    if (newPage >= 1 && newPage <= this.totalPages) {
      this.page = newPage;
      this.cdr.detectChanges();
    }
  }

  trackByPageIndex(index: number, item: number): number {
    return item;
  }

  supprimerCandidature(id?: number): void {
    if (!id) return;
    if (!confirm('Voulez-vous vraiment supprimer cette candidature ?')) return;

    this.candidatureService.supprimerCandidature(id).subscribe({
      next: () => {
        this.candidatures = this.candidatures.filter((c) => c.id !== id);
        this.applyFilter(false);
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de la suppression.',
        });
        console.error(err);
      },
    });
  }

  changerStatut(id?: number, nouveauStatut?: StatutCandidature): void {
    if (!id || !nouveauStatut) return;

    this.candidatureService.mettreAJourStatut(id, nouveauStatut).subscribe({
      next: (updated) => {
        const idx = this.candidatures.findIndex((c) => c.id === id);
        if (idx !== -1) {
          this.candidatures[idx].statut = updated.statut;
          this.applyFilter(false);
        }
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: `Statut mis à jour en "${updated.statut}".`,
          timer: 2000,
          showConfirmButton: false,
          timerProgressBar: true,
        });
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de la mise à jour du statut.',
        });
        console.error(err);
      },
    });
  }

  changerStatutEtEnvoyerEmail(
    candidature: Candidature & { fullImageUrl?: string }
  ): void {
    if (!candidature.id || !candidature.email) return;

    this.candidatureService
      .mettreAJourStatut(candidature.id, candidature.statut)
      .subscribe({
        next: (updated) => {
          const idx = this.candidatures.findIndex(
            (c) => c.id === candidature.id
          );
          if (idx !== -1) {
            this.candidatures[idx].statut = updated.statut;
            if (updated.dateEntretien) {
              this.candidatures[idx].dateEntretien = updated.dateEntretien;
            }
            this.applyFilter(false);
          }

          const datePourEmail = updated.dateEntretien ?? candidature.dateEntretien;

          console.log("Date entretien pour email:", datePourEmail);

          this.envoyerEmailStatut(
            candidature.email!,
            updated.statut!,
          );

          Swal.fire({
            icon: 'success',
            title: 'Succès',
            text: `Statut mis à jour en "${updated.statut}". Email envoyé.`,
            timer: 2500,
            showConfirmButton: false,
            timerProgressBar: true,
          });
        },
        error: (err) => {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: 'Erreur lors de la mise à jour du statut.',
          });
          console.error(err);
        },
      });
  }

  envoyerEmailStatut(email: string, statut: string): void {
    let corps = `Bonjour,

Votre candidature a été mise à jour avec le statut suivant : ${statut}.\n`;

    if (statut === 'ENTRETIEN_PLANIFIE' || statut === 'ACCEPTER') {
      corps += `
Vous recevrez prochainement un email contenant la date, l'heure et le lien d'accès à votre entretien.
Merci de bien vous préparer.
`;
    }

    corps += `

Cordialement,
L'équipe recrutement.`;

    const emailRequest: EmailRequest = {
      to: email,
      subject: 'Mise à jour de votre candidature',
      body: corps,
    };

    this.emailService.sendEmail(emailRequest).subscribe({
      next: () => {
        // Notification déjà gérée dans changerStatutEtEnvoyerEmail
      },
      error: (err) => {
        Swal.fire({
          icon: 'error',
          title: "Erreur",
          text: "Erreur lors de l'envoi de l'email",
        });
        console.error(err);
      },
    });
  }

  getBadgeClass(statut?: string): string {
    switch (statut) {
      case StatutCandidature.ACCEPTEE:
        return 'badge bg-success';
      case StatutCandidature.REFUSEE:
        return 'badge bg-danger';
      case StatutCandidature.EN_ATTENTE:
        return 'badge bg-warning text-dark';
      case StatutCandidature.ENTRETIEN_PLANIFIE:
        return 'badge bg-info text-dark';
      case StatutCandidature.ARCHIVEE:
        return 'badge bg-secondary';
      default:
        return 'badge bg-light text-dark';
    }
  }

  getFullImageUrl(imageProfilUrl?: string): string {
    if (!imageProfilUrl || imageProfilUrl.trim() === '') {
      return 'assets/avatar-default.png';
    }
    if (
      imageProfilUrl.startsWith('http://') ||
      imageProfilUrl.startsWith('https://')
    ) {
      return imageProfilUrl;
    }
    return `http://localhost:8089/${imageProfilUrl}`;
  }

  getDownloadUrl(relativePath?: string): string {
    if (!relativePath || relativePath.trim() === '') return '';
    if (relativePath.startsWith('http')) return relativePath;
    return `http://localhost:8089${relativePath}`;
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/avatar-default.png';
  }

  trackByCandidatureId(index: number, item: Candidature): number {
    return item.id ?? index;
  }

  envoyerEmail(email?: string): void {
    if (!email) return;
    // Cette méthode est dépréciée — tu peux l'enlever si tu veux.
  }

  modifierCandidature(candidature: Candidature): void {
    this.selectedCandidature = { ...candidature };
    this.modalModifierVisible = true;
  }

  fermerModal(): void {
    this.modalModifierVisible = false;
  }

  soumettreModification(): void {
    if (!this.selectedCandidature.id) return;

    const candidatureToUpdate: CandidatureUpdateDto = {
      email: this.selectedCandidature.email,
      telephone: this.selectedCandidature.telephone,
    };

    this.candidatureService
      .modifierCandidature(this.selectedCandidature.id, candidatureToUpdate)
      .subscribe({
        next: (updated) => {
          const idx = this.candidatures.findIndex((c) => c.id === updated.id);
          if (idx !== -1) {
            this.candidatures[idx] = {
              ...this.candidatures[idx],
              email: updated.email,
              telephone: updated.telephone,
              fullImageUrl: this.candidatures[idx].fullImageUrl,
            };
            this.applyFilter(false);
            this.cdr.detectChanges();
          }
          this.modalModifierVisible = false;
        },
        error: (err) => {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: 'Erreur lors de la modification',
          });
          console.error(err);
        },
      });
  }
}
