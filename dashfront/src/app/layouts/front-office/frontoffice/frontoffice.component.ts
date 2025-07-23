import { Component, OnInit, HostListener, ElementRef } from '@angular/core';
import { User } from 'app/models/user';
import { ActualiteService } from 'app/services/actualiteService/actualite.service';
import { EventService } from 'app/services/evenement.service';
import { AuthService } from 'app/services/userService/auth.service';
import { ReactionService } from 'app/services/reaction.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-frontoffice',
  templateUrl: './frontoffice.component.html',
  styleUrls: ['./frontoffice.component.css']
})
export class FrontofficeComponent implements OnInit {

  rendezvousList: any;
  rendezVousToday: any;
  connectedUserId: number | null = null;

  actualites: any[] = [];
  evenementsManuels: any[] = [];
  evenementsScrapes: any[] = [];

  commentaire: string = '';
  like: boolean = false;
  dislike: boolean = false;
  loggedInUserName: string = '';
  userRole: string = '';
  avatarUrl: string = 'assets/default-avatar.png';
  menuOpen = false;
  isLoggedIn = false;
  userStatus: string = '';
  userSubscription: any;
  act: any;

  notificationMessage: string = '';
  notificationType: 'success' | 'error' | '' = '';

  filteredRendezvous: any;
  forumsMatched: any; // si tu souhaites utiliser ce champ
goToRecommendationPage(): void {
  this.router.navigate(['/matching-forums']);
}

  constructor(
    private authService: AuthService,
    private elementRef: ElementRef,
    private actualiteService: ActualiteService,
    private eventService: EventService,
    private reactionService: ReactionService,
    private router: Router
  ) {}

  showNotification(message: string, type: 'success' | 'error') {
    this.notificationMessage = message;
    this.notificationType = type;
    setTimeout(() => {
      this.notificationMessage = '';
      this.notificationType = '';
    }, 3000);
  }

  // Méthode pour naviguer vers la page matching-forums
  goToMatchingForumsPage(): void {
    this.router.navigate(['/matching-forums']);
  }

  ngOnInit(): void {
    this.userSubscription = this.authService.currentUser$.subscribe((user: User | null) => {
      this.isLoggedIn = !!user;
      if (user) {
        const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
        this.loggedInUserName = fullName !== '' ? fullName : user.username || 'Utilisateur';
        this.userRole = (user.roles && user.roles.length > 0)
          ? (user.roles[0].name || 'Rôle inconnu').replace(/^ROLE_/, '').replace(/_/g, ' ')
          : 'Rôle inconnu';
        this.avatarUrl = user.avatarUrl || 'assets/default-avatar.png';
        this.connectedUserId = user.id;
      } else {
        this.loggedInUserName = 'John Doe';
        this.userRole = '';
        this.avatarUrl = 'assets/default-avatar.png';
        this.connectedUserId = null;
      }
    });

    this.loadActualites();
    this.loadEvenements();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-icon-wrapper')) {
      this.menuOpen = false;
    }
  }

  onLogout(): void {
    localStorage.clear();
    this.isLoggedIn = false;
    this.menuOpen = false;
    this.userStatus = 'disconnected';
    window.location.reload();
  }

  loadActualites() {
    this.actualiteService.getall().subscribe(
      (result: any[]) => {
        this.actualites = result;
        this.actualites.forEach((act) => {
          const id = act.idActualite;
          if (this.isLoggedIn && this.loggedInUserName && this.loggedInUserName !== 'John Doe') {
            this.reactionService.getReactionCounts(id).subscribe((counts) => {
              act.likes = counts.likes || 0;
              act.dislikes = counts.dislikes || 0;
            });
            this.reactionService.getUserReaction(id, this.loggedInUserName).subscribe(
              (reaction: boolean | null) => {
                act.userReaction = reaction;
              },
              (error) => {
                console.error('Erreur chargement réaction utilisateur :', error);
              }
            );
          } else {
            act.likes = 0;
            act.dislikes = 0;
            act.userReaction = null;
          }
        });
      },
      (error) => {
        console.error('Erreur chargement actualités:', error);
      }
    );
  }

  loadEvenements() {
    this.eventService.getAllEvenements().subscribe(
      (result: any[]) => {
        this.evenementsManuels = result.filter(ev => !ev.source || ev.source !== 'scraping');
        this.evenementsScrapes = result.filter(ev => ev.source === 'scraping');
        console.log('Événements manuels:', this.evenementsManuels);
        console.log('Événements scrappés:', this.evenementsScrapes);
      },
      (error) => {
        console.error('Erreur chargement événements:', error);
      }
    );
  }

  likeActualite(id: number): void {
    if (!this.loggedInUserName) {
      alert("Veuillez vous connecter pour liker.");
      return;
    }
    this.reactionService.addReaction(id, this.loggedInUserName, true).subscribe(
      () => {
        const act = this.actualites.find(a => a.idActualite === id);
        if (act) {
          if (act.userReaction !== true) {
            act.likes = (act.likes || 0) + 1;
            if (act.userReaction === false && act.dislikes > 0) {
              act.dislikes = act.dislikes - 1;
            }
            act.userReaction = true;
          }
        }
      },
      (error) => {
        console.error('Erreur Like:', error);
        alert('Erreur lors du like.');
      }
    );
  }

  dislikeActualite(id: number): void {
    if (!this.loggedInUserName) {
      alert("Veuillez vous connecter pour disliker.");
      return;
    }
    this.reactionService.addReaction(id, this.loggedInUserName, false).subscribe(
      () => {
        const act = this.actualites.find(a => a.idActualite === id);
        if (act) {
          if (act.userReaction !== false) {
            act.dislikes = (act.dislikes || 0) + 1;
            if (act.userReaction === true && act.likes > 0) {
              act.likes = act.likes - 1;
            }
            act.userReaction = false;
          }
        }
      },
      (error) => {
        console.error('Erreur Dislike:', error);
        alert('Erreur lors du dislike.');
      }
    );
  }

  addComment(idact: any) {
    if (!this.commentaire || this.commentaire.trim() === '') {
      alert('Veuillez saisir un commentaire.');
      return;
    }
    this.actualiteService.addComment(idact, this.commentaire).subscribe(
      () => {
        this.commentaire = '';
        this.loadActualites();
      },
      (error) => {
        console.error('Erreur ajout commentaire:', error);
        alert('Erreur lors de l\'ajout du commentaire : ' + JSON.stringify(error));
      }
    );
  }

  allerVersReservation(idEvent: number): void {
    if (!this.isLoggedIn) {
      this.showNotification("⚠️ Vous devez vous authentifier pour participer à l'événement", 'error');
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 2000);
      return;
    }
    this.router.navigate(['/front/reserver', idEvent]);
  }

  shareData: ShareData = {
    url: 'https://github.com/mohamedguesmii',
    description: 'dev',
    tags: 'hussein_AbdElaziz',
  };

  shareLinks: ShareLinks[] = [
    {
      title: 'fb',
      link: `https://www.facebook.com/sharer.php?u=${this.shareData?.url}`,
    },
  ];

}

type ShareData = { url: string; description: string; tags: string };
type ShareLinks = { title: string; link: string };
