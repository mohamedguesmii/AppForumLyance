import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Evenement } from 'app/models/evenement';
import { EventService } from 'app/services/evenement.service';
import { ReservationService } from 'app/services/reservation.service';
import { AuthService, User } from 'app/services/userService/auth.service';

@Component({
  selector: 'app-reserver',
  templateUrl: './reserver.component.html',
  styleUrls: ['./reserver.component.css']
})
export class ReserverComponent implements OnInit {
  evenement: Evenement | null = null;
  currentUser: User | null = null;
  showParticipantInfo = false;

  // Pour la recherche, pagination etc.
  searchTerm: string = '';
  currentPage: number = 1;
  itemsPerPage: number = 10;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private reservationService: ReservationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const eventId = this.route.snapshot.params['id'];
    this.loadEvenement(eventId);

    // S'abonner à l'utilisateur connecté
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;

      if (!user) {
        alert('Vous devez être connecté pour participer à un événement.');
        this.router.navigate(['/login']);
      }
    });
  }

  loadEvenement(id: number): void {
    this.eventService.getEvenementById(id).subscribe({
      next: (data: Evenement) => {
        this.evenement = data;
      },
      error: err => {
        console.error('Erreur lors du chargement de l’événement', err);
      }
    });
  }

  reserver(): void {
    if (!this.evenement || !this.currentUser) {
      alert('Événement ou utilisateur non disponible.');
      return;
    }

    this.reservationService.reserverEvenement(this.evenement.idevent, this.currentUser.id!).subscribe({
      next: () => {
        alert('Réservation réussie !');
        this.showParticipantInfo = true;
      },
      error: err => {
        alert('Erreur lors de la réservation : ' + (err.error?.message || err.message));
      }
    });
  }

  resetPagination(): void {
    this.currentPage = 1;
  }

  goToChat(): void {
    // Navigue vers la page chat, adapte le chemin selon ta route Angular
    this.router.navigate(['/chat']);
  }

  goToEmail(): void {
    // Navigue vers la page email, adapte le chemin selon ta route Angular
    this.router.navigate(['/email']);
  }

  goToHistorical(): void {
    // Navigue vers la page historique, adapte le chemin selon ta route Angular
    this.router.navigate(['/historique']);
  }
}
