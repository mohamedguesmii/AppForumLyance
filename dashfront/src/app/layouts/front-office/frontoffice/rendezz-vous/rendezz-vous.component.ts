import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RendezVousService } from 'app/services/rendez-vous.service';
import { RendezVousDTO } from 'app/models/RendezVousDTO';

@Component({
  selector: 'app-rendezz-vous',
  templateUrl: './rendezz-vous.component.html',
  styleUrls: ['./rendezz-vous.component.css']
})
export class RendezzVousComponent implements OnInit {

  rendezvousList: RendezVousDTO[] = [];
  isLoading = true;
  errorMessage: string | null = null;

  currentUserId: string | null = null;  // ID en string
  currentUserRole: string | null = null;

  constructor(
    private rendezVousService: RendezVousService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      const userParsed = JSON.parse(userJson);
      this.currentUserId = userParsed.id?.toString() ?? null;
      this.currentUserRole = userParsed.role ?? null;
      console.log('User chargé:', this.currentUserId, this.currentUserRole);
    }

    this.loadRendezVous();
  }

  loadRendezVous(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.rendezVousService.getRendezVousPlanifiesAvecCandidature().subscribe({
      next: (data) => {
        const now = new Date();

        // Filtrer rendez-vous avec statut PLANIFIE et dates futures
        let list = data.filter(rdv => rdv.statut === 'PLANIFIE' && new Date(rdv.dateHeure) > now);

        // Trier par date croissante
        list.sort((a, b) => new Date(a.dateHeure).getTime() - new Date(b.dateHeure).getTime());

        // Regrouper par jour
        const rdvsByDay: { [key: string]: RendezVousDTO[] } = {};
        list.forEach(rdv => {
          const dayKey = new Date(rdv.dateHeure).toISOString().substring(0, 10);
          if (!rdvsByDay[dayKey]) rdvsByDay[dayKey] = [];
          rdvsByDay[dayKey].push(rdv);
        });

        // Répartir les heures (9h, 10h, ...)
        Object.values(rdvsByDay).forEach(rdvList => {
          rdvList.forEach((rdv, index) => {
            const newDate = new Date(rdv.dateHeure);
            newDate.setHours(9 + index, 0, 0, 0);
            rdv.dateHeure = newDate.toISOString();
          });
        });

        this.rendezvousList = Object.values(rdvsByDay).flat();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement des rendez-vous';
        this.isLoading = false;
      }
    });
  }

  isCurrentUserOwner(rdv: RendezVousDTO): boolean {
    const currentUserIdStr = this.currentUserId ?? '';
    const rdvUserIdStr = rdv.candidature?.idUser?.toString() ?? '';
    console.log('Comparaison IDs:', { currentUserIdStr, rdvUserIdStr });
    return currentUserIdStr === rdvUserIdStr;
  }

  navigateToEntretienvisio(token: string): void {
    if (!token) {
      console.error('Token manquant pour navigation vers entretien');
      return;
    }

    const rdv = this.rendezvousList.find(r => r.tokenAcces === token || r.token === token);
    if (!rdv) {
      alert('Rendez-vous introuvable.');
      return;
    }

    if (!this.isCurrentUserOwner(rdv)) {
      alert('Vous ne pouvez accéder qu’à votre propre rendez-vous.');
      return;
    }

    this.router.navigate(['/entretien', token]);
  }

  formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR') + ' ' + date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
}
