import { Component, OnInit } from '@angular/core';
import { Offre, OffreService } from 'app/services/offre.service';
import { EventService } from 'app/services/evenement.service';
import { Evenement } from 'app/models/evenement';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-offre',
  templateUrl: './offre.component.html',
  styleUrls: ['./offre.component.css']
})
export class OffreComponent implements OnInit {

  private _searchTerm: string = '';
  pageSize: number = 5;

  emploiPage: number = 1;
  stagePage: number = 1;

  offresEmploi: Offre[] = [];
  offresStage: Offre[] = [];
  evenements: Evenement[] = [];

  evenementMap: Map<number, string> = new Map();

  showForm: boolean = false;
  offreEnEdition: Offre | null = null;

  constructor(
    private offreService: OffreService,
    private evenementService: EventService
  ) {}

  ngOnInit(): void {
    this.loadEvenements();
  }

  get searchTerm(): string {
    return this._searchTerm;
  }
  set searchTerm(value: string) {
    this._searchTerm = value;
    this.resetPagination();
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
        this.loadOffres();
      },
      error: (err) => {
        console.error('Erreur chargement événements:', err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors du chargement des événements.'
        });
      }
    });
  }

  loadOffres(): void {
    this.offreService.getAllOffres().subscribe({
      next: (data) => {
        this.offresEmploi = data.filter(o => o.type === 'emploi');
        this.offresStage = data.filter(o => o.type === 'stage');
      },
      error: (err) => {
        console.error('Erreur chargement offres:', err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors du chargement des offres.'
        });
      }
    });
  }

  getEvenementTitre(evenementId: number | string | null | undefined): string {
    if (evenementId == null) return '';
    const idNum = Number(evenementId);
    if (isNaN(idNum)) return '';
    return this.evenementMap.get(idNum) ?? '';
  }

  get filteredEmploi(): Offre[] {
    const term = this.searchTerm.toLowerCase();
    return this.offresEmploi.filter(o =>
      (o.titre?.toLowerCase() ?? '').includes(term) ||
      (o.description?.toLowerCase() ?? '').includes(term)
    );
  }

  get filteredStage(): Offre[] {
    const term = this.searchTerm.toLowerCase();
    return this.offresStage.filter(o =>
      (o.titre?.toLowerCase() ?? '').includes(term) ||
      (o.description?.toLowerCase() ?? '').includes(term)
    );
  }

  get pagedEmploi(): Offre[] {
    const start = (this.emploiPage - 1) * this.pageSize;
    return this.filteredEmploi.slice(start, start + this.pageSize);
  }

  get pagedStage(): Offre[] {
    const start = (this.stagePage - 1) * this.pageSize;
    return this.filteredStage.slice(start, start + this.pageSize);
  }

  get totalEmploiPages(): number {
    return Math.max(1, Math.ceil(this.filteredEmploi.length / this.pageSize));
  }

  get totalStagePages(): number {
    return Math.max(1, Math.ceil(this.filteredStage.length / this.pageSize));
  }

  resetPagination() {
    this.emploiPage = 1;
    this.stagePage = 1;
  }

  emploiPrevPage() {
    if (this.emploiPage > 1) this.emploiPage--;
  }
  emploiNextPage() {
    if (this.emploiPage < this.totalEmploiPages) this.emploiPage++;
  }

  stagePrevPage() {
    if (this.stagePage > 1) this.stagePage--;
  }
  stageNextPage() {
    if (this.stagePage < this.totalStagePages) this.stagePage++;
  }

  private initOffre(): Offre {
    return {
      id: null,
      titre: '',
      description: '',
      domaine: '',
      datePublication: new Date().toISOString().substring(0, 10),
      statut: 'EN_ATTENTE',
      type: 'emploi',
      duree: '',
      evenementId: null,
      createurId: 1
    };
  }

  ajouterOffre() {
    this.offreEnEdition = this.initOffre();
    this.showForm = true;
  }

  modifierOffre(offre: Offre) {
    this.offreEnEdition = { ...offre };
    this.showForm = true;
  }

  supprimerOffre(offre: Offre) {
    Swal.fire({
      title: `Confirmer la suppression de l'offre : ${offre.titre} ?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Oui, supprimer',
      cancelButtonText: 'Annuler'
    }).then((result) => {
      if (result.isConfirmed) {
        this.offreService.deleteOffre(offre.id!).subscribe({
          next: () => {
            if (offre.type === 'emploi') {
              this.offresEmploi = this.offresEmploi.filter(o => o.id !== offre.id);
            } else {
              this.offresStage = this.offresStage.filter(o => o.id !== offre.id);
            }
            Swal.fire({
              icon: 'success',
              title: 'Supprimé',
              text: 'Offre supprimée avec succès',
              timer: 1500,
              showConfirmButton: false
            });
          },
          error: (err) => {
            console.error('Erreur suppression offre:', err);
            Swal.fire({
              icon: 'error',
              title: 'Erreur',
              text: 'Erreur lors de la suppression de l\'offre.'
            });
          }
        });
      }
    });
  }

  soumettreModification() {
    if (!this.offreEnEdition) return;

    const payload: any = { ...this.offreEnEdition };

    if (payload.evenementId !== null) payload.evenementId = Number(payload.evenementId);
    payload.createurId = Number(payload.createurId);

    const statutMapping: { [key: string]: string } = {
      'EN_ATTENTE': 'en attente',
      'ENATTENTE': 'en attente',
      'EN-ATTENTE': 'en attente',
      'EN ATTENTE': 'en attente',
      'ENattente': 'en attente',

      'ACCEPTEE': 'acceptée',
      'ACCEPTÉE': 'acceptée',
      'ACCEPTÉ': 'acceptée',

      'REFUSEE': 'refusée',
      'REFUSÉE': 'refusée',
      'REFUSE': 'refusée'
    };

    if (payload.statut) {
      const key = payload.statut.toUpperCase().replace(/[_\s-]/g, '');
      payload.statut = statutMapping[key] ?? payload.statut.toLowerCase();
    } else {
      payload.statut = 'en attente';
    }

    Object.keys(payload).forEach(key => {
      if (payload[key] === '' || payload[key] === undefined || payload[key] === null) {
        delete payload[key];
      }
    });

    if (payload.id) {
      this.offreService.updateOffre(payload.id, payload).subscribe({
        next: (offreModifiee) => {
          if (offreModifiee.type === 'emploi') {
            const index = this.offresEmploi.findIndex(o => o.id === offreModifiee.id);
            if (index !== -1) this.offresEmploi[index] = offreModifiee;
          } else {
            const index = this.offresStage.findIndex(o => o.id === offreModifiee.id);
            if (index !== -1) this.offresStage[index] = offreModifiee;
          }
          Swal.fire({
            icon: 'success',
            title: 'Modifié',
            text: 'Offre modifiée avec succès',
            timer: 1500,
            showConfirmButton: false
          });
          this.fermerFormulaire();
        },
        error: () => {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: 'Erreur lors de la modification.'
          });
        }
      });
    } else {
      this.offreService.ajouterOffre(payload).subscribe({
        next: (offreAjoutee) => {
          if (offreAjoutee.type === 'emploi') {
            this.offresEmploi.unshift(offreAjoutee);
          } else {
            this.offresStage.unshift(offreAjoutee);
          }
          Swal.fire({
            icon: 'success',
            title: 'Ajouté',
            text: 'Offre ajoutée avec succès',
            timer: 1500,
            showConfirmButton: false
          });
          this.fermerFormulaire();
        },
        error: () => {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: 'Erreur lors de l\'ajout de l\'offre'
          });
        }
      });
    }
  }

  annulerModification() {
    this.fermerFormulaire();
  }

  private fermerFormulaire() {
    this.offreEnEdition = null;
    this.showForm = false;
  }
}
