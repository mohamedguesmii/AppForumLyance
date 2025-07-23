import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { RendezVousService } from 'app/services/rendez-vous.service';
import { RendezVousDTO } from '../models/RendezVousDTO';
import { EmailEntretienRequest } from 'app/models/email-entretien-request';

@Component({
  selector: 'app-rendez-vous',
  templateUrl: './rendez-vous.component.html',
  styleUrls: ['./rendez-vous.component.css']
})
export class RendezVousComponent implements OnInit {

  rendezvousList: RendezVousDTO[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  filterStatut: string = '';
  searchText: string = '';

  selectedRdvToEdit: RendezVousDTO | null = null;

  selectedIds: Set<number> = new Set<number>();

  @ViewChild('editFormContainer') editFormContainer!: ElementRef;

  messageEmail: string = '';
  http: any;

  constructor(private rendezVousService: RendezVousService) { }

  ngOnInit(): void {
    this.loadRendezVous();
  }

  loadRendezVous(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rendezVousService.getRendezVousPlanifiesAvecCandidature().subscribe({
      next: (data) => {
        this.rendezvousList = data.map(rdv => ({ ...rdv, showDropdown: false }));
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement des rendez-vous';
        this.isLoading = false;
      }
    });
  }

  get filteredRendezvous(): RendezVousDTO[] {
    let list = this.rendezvousList;

    if (this.filterStatut) {
      list = list.filter(r => r.statut === this.filterStatut);
    }

    if (this.searchText.trim() !== '') {
      const normalize = (str: string) =>
        str.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

      const searchNormalized = normalize(this.searchText.trim());

      list = list.filter(rdv => {
        if (!rdv.candidature) return false;
        return ['prenom', 'nom', 'email'].some(field => {
          const value = rdv.candidature[field];
          return value && normalize(value).includes(searchNormalized);
        });
      });
    }

    const now = new Date();
    list = list.filter(rdv => {
      if (!rdv.dateHeure) return false;
      return new Date(rdv.dateHeure) > now;
    });

    list = list.sort((a, b) => new Date(a.dateHeure).getTime() - new Date(b.dateHeure).getTime());

    const rdvsByDay: { [key: string]: RendezVousDTO[] } = {};

    list.forEach(rdv => {
      const dt = new Date(rdv.dateHeure);
      const dayKey = dt.toISOString().substring(0, 10);

      if (!rdvsByDay[dayKey]) {
        rdvsByDay[dayKey] = [];
      }
      rdvsByDay[dayKey].push(rdv);
    });

    Object.values(rdvsByDay).forEach(rdvList => {
      rdvList.forEach((rdv, index) => {
        const originalDate = new Date(rdv.dateHeure);
        const newDate = new Date(originalDate);
        newDate.setHours(9 + index, 0, 0, 0);
        rdv.dateHeure = newDate.toISOString();
      });
    });

    const result = Object.values(rdvsByDay).flat();

    result.sort((a, b) => new Date(a.dateHeure).getTime() - new Date(b.dateHeure).getTime());

    return result;
  }

  toggleSelection(id: number): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  isAllSelected(): boolean {
    const filteredIds = this.filteredRendezvous.map(r => r.id);
    return filteredIds.length > 0 && filteredIds.every(id => this.selectedIds.has(id));
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) {
      this.filteredRendezvous.forEach(r => this.selectedIds.delete(r.id));
    } else {
      this.filteredRendezvous.forEach(r => this.selectedIds.add(r.id));
    }
  }

  supprimerSelectionMultiple(): void {
    if (this.selectedIds.size === 0) {
      alert('Aucun rendez-vous sélectionné.');
      return;
    }

    if (!confirm('Êtes-vous sûr de vouloir supprimer les rendez-vous sélectionnés ?')) {
      return;
    }

    const idsToDelete = Array.from(this.selectedIds);
    let deletedCount = 0;

    idsToDelete.forEach(id => {
      this.rendezVousService.supprimerRendezVous(id).subscribe({
        next: () => {
          this.rendezvousList = this.rendezvousList.filter(r => r.id !== id);
          this.selectedIds.delete(id);
          deletedCount++;
          if (deletedCount === idsToDelete.length) {
            this.successMessage = 'Rendez-vous sélectionnés supprimés avec succès.';
            setTimeout(() => this.successMessage = null, 3000);
          }
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          this.errorMessage = 'Erreur lors de la suppression de certains rendez-vous';
          setTimeout(() => this.errorMessage = null, 3000);
        }
      });
    });
  }

  modifierRendezVous(rdv: RendezVousDTO): void {
    this.selectedRdvToEdit = { ...rdv };

    if (this.selectedRdvToEdit.dateHeure) {
      const d = new Date(this.selectedRdvToEdit.dateHeure);
      const pad = (n: number) => n.toString().padStart(2, '0');
      this.selectedRdvToEdit.dateHeure = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }

    setTimeout(() => {
      if (this.editFormContainer) {
        this.editFormContainer.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  annulerModification(): void {
    this.selectedRdvToEdit = null;
  }

  sauvegarderModification(updatedRdv: RendezVousDTO): void {
    if (!updatedRdv.id) return;

    if (typeof updatedRdv.dateHeure === 'string' && !updatedRdv.dateHeure.endsWith('Z')) {
      const localDate = new Date(updatedRdv.dateHeure);
      updatedRdv.dateHeure = localDate.toISOString();
    }

    this.rendezVousService.updateRendezVous(updatedRdv.id, updatedRdv).subscribe({
      next: (rdvMisAJour) => {
        const index = this.rendezvousList.findIndex(r => r.id === rdvMisAJour.id);
        if (index !== -1) {
          this.rendezvousList[index] = { ...this.rendezvousList[index], ...rdvMisAJour };
        }
        this.selectedRdvToEdit = null;
        this.errorMessage = null;
        this.successMessage = 'Rendez-vous mis à jour avec succès.';
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors de la mise à jour du rendez-vous';
        this.successMessage = null;
        console.error(err);
      }
    });
  }

  supprimerRendezVous(id: number): void {
    this.errorMessage = null;
    if (confirm('Êtes-vous sûr de vouloir supprimer ce rendez-vous ?')) {
      this.rendezVousService.supprimerRendezVous(id).subscribe({
        next: (responseText) => {
          this.rendezvousList = this.rendezvousList.filter(r => r.id !== id);
          this.selectedIds.delete(id);
          this.successMessage = responseText || 'Rendez-vous supprimé avec succès.';
          this.errorMessage = null;
          setTimeout(() => this.successMessage = null, 3000);
        },
        error: (err) => {
          console.error('Erreur lors de la suppression:', err);
          this.errorMessage = 'Erreur lors de la suppression';
          this.successMessage = null;
        }
      });
    }
  }

  changerStatut(rdv: RendezVousDTO, nouveauStatut: string): void {
    this.rendezVousService.modifierStatutRendezVous(rdv.id, nouveauStatut).subscribe({
      next: () => {
        rdv.statut = nouveauStatut;
        this.errorMessage = null;
        this.successMessage = 'Statut modifié avec succès.';
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors de la modification du statut';
        this.successMessage = null;
        console.error(err);
      }
    });
  }

  onDateChange(newDate: string) {
    if (!this.selectedRdvToEdit?.dateHeure) {
      this.selectedRdvToEdit!.dateHeure = newDate + 'T00:00:00Z';
    } else {
      const current = new Date(this.selectedRdvToEdit.dateHeure);
      const [year, month, day] = newDate.split('-').map(Number);
      current.setFullYear(year, month - 1, day);
      this.selectedRdvToEdit.dateHeure = current.toISOString();
    }
  }

  onTimeChange(newTime: string) {
    if (!this.selectedRdvToEdit?.dateHeure) {
      this.selectedRdvToEdit!.dateHeure = '1970-01-01T' + newTime + ':00Z';
    } else {
      const current = new Date(this.selectedRdvToEdit.dateHeure);
      const [hours, minutes] = newTime.split(':').map(Number);
      current.setHours(hours, minutes, 0);
      this.selectedRdvToEdit.dateHeure = current.toISOString();
    }
  }

  // ---------------------- AJOUT : Fonction d'envoi d'email ----------------------
  
envoyerEmail(rdv: any) {
  console.log('Rendez-vous pour envoi email:', rdv);

  if (!rdv.candidature?.email) {
    alert('Email candidat manquant.');
    return;
  }

  if (!rdv.dateHeure) {
    alert('Date et heure du rendez-vous manquantes.');
    return;
  }

  if (!rdv.tokenAcces) {
    alert('Token manquant dans le rendez-vous, impossible de construire le lien.');
    return;
  }

  rdv.lienVisio = `http://localhost:4200/#/entretien/${rdv.tokenAcces}`;
  console.warn('Lien visio utilisé:', rdv.lienVisio);

  const payload = {
    email: rdv.candidature.email,
    dateHeure: rdv.dateHeure,
    lienVisio: rdv.lienVisio
  };

  console.log('Payload envoyé à l’API :', payload);

  this.rendezVousService.envoyerEmailEntretien(payload).subscribe({
    next: (res) => alert(res.message || 'Email envoyé avec succès.'),
    error: (err) => {
      console.error('Erreur envoi email:', err);
      alert('Erreur lors de l\'envoi de l\'email : ' + (err?.error?.error || err?.message));
    }
  });
}


}
