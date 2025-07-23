import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TokenStorageService } from 'app/services/userService/token-storage.service';
import { OffreService, Offre } from 'app/services/offre.service';
import { EventService } from 'app/services/evenement.service';
import { CandidatureService } from 'app/services/candidature.service';
import { Evenement } from 'app/models/evenement';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  offres: Offre[] = [];
  filteredOffres: Offre[] = [];

  evenements: Evenement[] = [];
  evenementMap: Map<number, string> = new Map();

  offresPostulees: number[] = []; // Offres déjà postulées

  isLoggedIn = false;
  private roles: string[] = [];
  username?: string;
  userId?: number;
  searchTerm: string = '';

  // Pagination Emploi
  emploiPage: number = 1;
  emploiPageSize: number = 2;

  // Pagination Stage
  stagePage: number = 1;
  stagePageSize: number = 2;

  constructor(
    private tokenStorageService: TokenStorageService,
    private router: Router,
    private offreService: OffreService,
    private evenementService: EventService,
    private candidatureService: CandidatureService
  ) {}

  ngOnInit(): void {
  this.isLoggedIn = !!this.tokenStorageService.getToken();

  if (this.isLoggedIn) {
    const user = this.tokenStorageService.getUser();
    if (user && user.roles) {
      this.roles = user.roles;
      this.username = user.username;
      this.userId = user.id;

      // Charger les offres postulées
      this.loadOffresPostulees(this.userId);
    }
  }

  this.loadEvenements();
  this.loadOffres();

  window.scrollTo(0, 0);
}

loadOffresPostulees(userId: number): void {
  this.candidatureService.getOffresPostuleesParCandidat(userId).subscribe({
    next: (ids) => {
      this.offresPostulees = ids;
    },
    error: (err) => {
      console.error('Erreur chargement des candidatures :', err);
    }
  });
}


  loadEvenements(): void {
    this.evenementService.getAllEvenements().subscribe({
      next: (data) => {
        this.evenements = data;
        this.evenementMap.clear();
        this.evenements.forEach(ev => {
          if (ev.idevent != null) {
            this.evenementMap.set(ev.idevent, ev.title);
          }
        });
      },
      error: (err) => {
        console.error('Erreur chargement événements:', err);
      }
    });
  }

  loadOffres(): void {
    this.offreService.getAllOffres().subscribe({
      next: (data) => {
        this.offres = data;
        this.applyFilter();
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des offres', err);
      }
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      this.filteredOffres = [...this.offres];
    } else {
      this.filteredOffres = this.offres.filter(offre =>
        (offre.titre?.toLowerCase().includes(term) ?? false) ||
        (offre.domaine?.toLowerCase().includes(term) ?? false)
      );
    }
    this.emploiPage = 1;
    this.stagePage = 1;
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  get offresEmploi(): Offre[] {
    const filtered = this.filteredOffres.filter(o => o.type === 'emploi');
    return this.paginate(filtered, this.emploiPage, this.emploiPageSize);
  }

  get offresStage(): Offre[] {
    const filtered = this.filteredOffres.filter(o => o.type === 'stage');
    return this.paginate(filtered, this.stagePage, this.stagePageSize);
  }

  paginate(array: Offre[], page: number, pageSize: number): Offre[] {
    const start = (page - 1) * pageSize;
    return array.slice(start, start + pageSize);
  }

  get emploiTotalPages(): number {
    const count = this.filteredOffres.filter(o => o.type === 'emploi').length;
    return Math.max(Math.ceil(count / this.emploiPageSize), 1);
  }

  get stageTotalPages(): number {
    const count = this.filteredOffres.filter(o => o.type === 'stage').length;
    return Math.max(Math.ceil(count / this.stagePageSize), 1);
  }

  emploiPreviousPage(): void {
    if (this.emploiPage > 1) {
      this.emploiPage--;
    }
  }

  emploiNextPage(): void {
    if (this.emploiPage < this.emploiTotalPages) {
      this.emploiPage++;
    }
  }

  stagePreviousPage(): void {
    if (this.stagePage > 1) {
      this.stagePage--;
    }
  }

  stageNextPage(): void {
    if (this.stagePage < this.stageTotalPages) {
      this.stagePage++;
    }
  }

  getEvenementTitre(evenementId: number | null | undefined): string {
    if (evenementId == null) return '';
    const idNum = Number(evenementId);
    if (isNaN(idNum)) return '';
    return this.evenementMap.get(idNum) ?? '';
  }

  /** Retourne vrai si l'utilisateur a déjà postulé à cette offre */
  hasAlreadyApplied(offreId: number): boolean {
    return this.offresPostulees.includes(offreId);
  }

  postuler(offreId: number): void {
    if (this.hasAlreadyApplied(offreId)) {
      alert('Vous avez déjà postulé à cette offre.');
      return;
    }
    console.log('Redirection vers addcandidature avec offreId =', offreId);
    this.router.navigate(['/front/addcandidature'], { queryParams: { offreId } });
  }

  goToEmail(): void {
    this.router.navigate(['/email']);
  }

  goToFiles(): void {
    this.router.navigate(['/files']);
  }

  goToHistorical(): void {
    this.router.navigate(['/historique']);
  }

  goBack(): void {
    this.router.navigate(['/front']);
  }

  goToChat(): void {
    this.router.navigate(['/chat2']);
  }

  goToResultat(userId: number): void {
    this.router.navigate(['/resultat', userId]);
  }

}
