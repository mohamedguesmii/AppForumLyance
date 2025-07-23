import { Component, OnInit, ElementRef, OnDestroy } from '@angular/core';
import { ROUTES } from '../sidebar/sidebar.component';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from 'app/services/userService/auth.service';
import { User } from 'app/models/user';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  toggleNotifications($event: any) {
    throw new Error('Method not implemented.');
  }
  onSearch() {
    throw new Error('Method not implemented.');
  }

  searchQuery: any;
  private listTitles: any[];
  location: Location;
  mobile_menu_visible: any = 0;
  private toggleButton: any;
  private sidebarVisible: boolean;
  notificationsOpen: any;

  loggedInUserName: string = 'John Doe';
  userRole: string = '';
  avatarUrl: string = 'assets/default-avatar.png'; // avatar par défaut
  menuOpen: boolean = false; // pour le menu dropdown de l'icône person
  user: User | null = null;

  private userSubscription: Subscription;

  constructor(
    location: Location,
    private element: ElementRef,
    private router: Router,
    private authService: AuthService
  ) {
    this.location = location;
    this.sidebarVisible = false;
  }

  ngOnInit() {
    this.listTitles = ROUTES.filter(listTitle => listTitle);
    const navbar: HTMLElement = this.element.nativeElement;
    this.toggleButton = navbar.getElementsByClassName('navbar-toggler')[0];

    // S'abonner aux données utilisateur
    this.userSubscription = this.authService.currentUser$.subscribe((user: User | null) => {
      console.log("✅ Utilisateur connecté :", user);

      if (user) {
        const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
        this.loggedInUserName = fullName !== '' ? fullName : user.username || 'Utilisateur';

        this.userRole = (user.roles && user.roles.length > 0)
          ? (user.roles[0].name || 'Rôle inconnu')
            .replace(/^ROLE_/, '')
            .replace(/_/g, ' ')
          : 'Rôle inconnu';

        this.avatarUrl = user.avatarUrl || 'assets/default-avatar.png';
        this.user = user;
      } else {
        this.loggedInUserName = 'John Doe';
        this.userRole = '';
        this.avatarUrl = 'assets/default-avatar.png';
        this.user = null;
      }
    });

    this.router.events.subscribe(() => {
      this.sidebarClose();
      const layer: any = document.getElementsByClassName('close-layer')[0];
      if (layer) {
        layer.remove();
        this.mobile_menu_visible = 0;
      }
    });
  }

  ngOnDestroy() {
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  sidebarOpen() {
    const toggleButton = this.toggleButton;
    const body = document.getElementsByTagName('body')[0];
    setTimeout(() => {
      toggleButton.classList.add('toggled');
    }, 500);

    body.classList.add('nav-open');
    this.sidebarVisible = true;
  }

  sidebarClose() {
    const body = document.getElementsByTagName('body')[0];
    this.toggleButton.classList.remove('toggled');
    this.sidebarVisible = false;
    body.classList.remove('nav-open');
  }

  sidebarToggle() {
    const toggle = this.toggleButton;
    const body = document.getElementsByTagName('body')[0];

    if (!this.sidebarVisible) {
      this.sidebarOpen();
    } else {
      this.sidebarClose();
    }

    if (this.mobile_menu_visible === 1) {
      body.classList.remove('nav-open');
      const layer: any = document.getElementsByClassName('close-layer')[0];
      if (layer) {
        layer.remove();
      }
      setTimeout(() => {
        toggle.classList.remove('toggled');
      }, 400);

      this.mobile_menu_visible = 0;
    } else {
      setTimeout(() => {
        toggle.classList.add('toggled');
      }, 430);

      const layer = document.createElement('div');
      layer.setAttribute('class', 'close-layer');

      if (body.querySelectorAll('.main-panel').length > 0) {
        document.getElementsByClassName('main-panel')[0].appendChild(layer);
      } else if (body.classList.contains('off-canvas-sidebar')) {
        document.getElementsByClassName('wrapper-full-page')[0].appendChild(layer);
      }

      setTimeout(() => {
        layer.classList.add('visible');
      }, 100);

      layer.onclick = () => {
        body.classList.remove('nav-open');
        this.mobile_menu_visible = 0;
        layer.classList.remove('visible');
        setTimeout(() => {
          layer.remove();
          toggle.classList.remove('toggled');
        }, 400);
      };

      body.classList.add('nav-open');
      this.mobile_menu_visible = 1;
    }
  }

  getTitle() {
    let titlee = this.location.prepareExternalUrl(this.location.path());
    if (titlee.charAt(0) === '#') {
      titlee = titlee.slice(1);
    }

    for (let item = 0; item < this.listTitles.length; item++) {
      if (this.listTitles[item].path === titlee) {
        return this.listTitles[item].title;
      }
    }
    return 'Dashboard';
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  onLogout() {
    console.log('Logout clicked');
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
