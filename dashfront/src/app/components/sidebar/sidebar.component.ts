import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';  // <-- Import Router Angular
import { AuthService } from 'app/services/userService/auth.service';

declare interface RouteInfo {
  path: string;
  title: string;
  icon: string;
  class: string;
  children?: RouteInfo[];
  expanded?: boolean;
  roles?: string[];
  action?: string;  // Permet d'avoir des actions comme "logout"
}


 export const ROUTES: RouteInfo[] = [
  { 
    path: '/dashboard', 
    title: 'Dashboard', 
    icon: 'dashboard', 
    class: '', 
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_CANDIDAT', 'ROLE_PARTENAIRE_EXTERNE'] 
  },
  {
    path: '/user',
    title: 'Gestion utilisateur',
    icon: 'group',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR'],
    children: [
      { path: '/user', title: 'Liste utilisateurs', icon: 'list', class: '', roles: ['ROLE_ADMINISTRATEUR'] },
      { path: '/adduser', title: 'Ajouter utilisateur', icon: 'person_add', class: '', roles: ['ROLE_ADMINISTRATEUR'] },
      { path: '/statistiques', title: 'Statistiques ', icon: 'bar_chart', class: '', roles: ['ROLE_ADMINISTRATEUR'] }
    ],
    expanded: false
  },
  {
    path: '/Gestiondesevenements',
    title: 'Gestion Evenement',
    icon: 'event',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_CANDIDAT'],
    children: [
      { path: '/Gestiondesevenements', title: 'Liste des événements', icon: 'list', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH'] },
      { path: '/Evenementsvenir', title: 'Événements à venir', icon: 'event_available', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_CANDIDAT'] }
    ],
    expanded: false
  },
  {
    path: '/Gestiondescandidatures',
    title: 'Gestion candidature',
    icon: 'assignment_ind',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH']
  },
  {
    path: '/Gestiondesoffres',
    title: 'Gestion offre',
    icon: 'sell',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_CANDIDAT', 'ROLE_PARTENAIRE_EXTERNE'],
    children: [
      { path: '/Gestiondesoffres', title: 'Liste des offres', icon: 'list', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_CANDIDAT', 'ROLE_PARTENAIRE_EXTERNE'] },
      { path: '/AddoffreComponent', title: 'Ajouter offre', icon: 'add_circle_outline', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_PARTENAIRE_EXTERNE'] }
    ],
    expanded: false
  },
  {
    path: '/RendezVousComponent',
    title: 'Gestion rendez-vous',
    icon: 'content_paste',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_PARTENAIRE_EXTERNE', 'ROLE_RESPONSABLE_RH']
  },
  {
    path: '/Gestiondesactualites',
    title: 'Gestion Actualite',
    icon: 'bubble_chart',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH'],
    children: [
      { path: '/Gestiondesactualites', title: 'Liste actualités', icon: 'list', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH'] },
      { path: '/impression-actualites', title: 'Impression', icon: 'thumb_up', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH', 'ROLE_CANDIDAT'] }
    ],
    expanded: false
  },
  {
    path: '/Gestiondesreservations',
    title: 'Gestion Reservation',
    icon: 'library_books',
    class: '',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH'],
    children: [
      { path: '/Gestiondesreservations', title: 'Liste participants', icon: 'list', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH'] },
      { path: '/reservation/statistiques', title: 'Statistique ', icon: 'bar_chart', class: '', roles: ['ROLE_ADMINISTRATEUR', 'ROLE_RESPONSABLE_RH'] }
    ],
    expanded: false
  },
  {
    path: '/validation',
    title: 'Gestion Workflow',
    icon: 'unarchive',
    class: 'active-pro',
    roles: ['ROLE_ADMINISTRATEUR', 'ROLE_PARTENAIRE_EXTERNE']
  },
  {
    action: 'logout',
    title: 'Déconnexion',
    icon: 'logout',
    path: '',
    class: '',
  }
];




@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {

  menuItems: RouteInfo[] = [];
  userRole: string = '';

  constructor(
    private authService: AuthService,
    private router: Router  // <-- Injection du Router Angular
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    console.log('Rôle récupéré dans sidebar:', this.userRole);

    const role = this.userRole?.toUpperCase();

    if (role.includes('ADMINISTRATEUR')) {
      this.menuItems = ROUTES.map(menu => ({
        ...menu,
        children: menu.children ? [...menu.children] : undefined,
        expanded: menu.children ? false : undefined
      }));
    } else if (role.includes('PARTENAIRE_EXTERNE')) {
      const allowedPaths = ['/dashboard', '/Gestiondesoffres', '/ticket'];
      this.menuItems = ROUTES
        .filter(m => allowedPaths.includes(m.path))
        .map(menu => ({
          ...menu,
          children: menu.children ? [...menu.children] : undefined,
          expanded: menu.children ? false : undefined
        }));
    } else if (role.includes('RESPONSABLE_RH')) {
      const allowedPaths = [
        '/dashboard',
        '/Gestiondesoffres',
        '/ticket',
        '/Gestiondesactualites',
        '/Gestiondescandidatures',
        '/Gestiondesreservations'
      ];
      this.menuItems = ROUTES
        .filter(m => allowedPaths.includes(m.path))
        .map(menu => ({
          ...menu,
          children: menu.children ? [...menu.children] : undefined,
          expanded: menu.children ? false : undefined
        }));
    } else {
      // CANDIDAT ou autre
      this.menuItems = ROUTES
        .filter(m => m.path === '/dashboard')
        .map(menu => ({
          ...menu,
          expanded: undefined
        }));
    }
  }

  
  toggleSubMenu(menuItem: RouteInfo): void {
    menuItem.expanded = !menuItem.expanded;
  }

  isMenuVisible(menu: RouteInfo): boolean {
    if (!menu.roles) return true;
    return menu.roles.includes(this.userRole);
  }

  isMobileMenu(): boolean {
    return window.innerWidth <= 991;
  }

  closeMenu() {
    // Implémenter si nécessaire, sinon laisse vide
  }

  onLinkClick(link: RouteInfo): void {
    if (link.action === 'logout') {
      this.onLogout();
    } else {
      this.closeMenu();
    }
  }

  onLogout(): void {
    this.authService.logout(); // Supprime token + reset user
    this.router.navigate(['/login']); // Redirection vers la page login
  }
}
