import { Component, OnInit } from '@angular/core';
import { UserService } from 'app/services/userService/user.service';
import { User } from 'app/models/user';

interface CardStat {
  title: string;
  total_no: string;
  image: string;
  description?: string;
}

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.css']
})
export class StatisticsComponent implements OnInit {

  cards: CardStat[] = [];

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.userService.getAll().subscribe({
      next: (users: User[]) => {
        const totalUsers = users.length;
        const totalAdmins = users.filter(u => u.role === 'ADMIN').length;
        const totalCandidats = users.filter(u => u.role === 'CANDIDAT').length;
        const totalRH = users.filter(u => u.role === 'RESPONSABLE_RH').length;
        const totalExternes = users.filter(u => u.role === 'PARTENAIRE_EXTERNE').length;

        this.cards = [
          {
            title: 'Utilisateurs',
            total_no: totalUsers.toString(),
            image: 'assets/images/svg/users.svg',
            description: '<span class="text-success">+5% ce mois</span>'
          },
          {
            title: 'Admins',
            total_no: totalAdmins.toString(),
            image: 'assets/images/svg/admin.svg'
          },
          {
            title: 'Candidats',
            total_no: totalCandidats.toString(),
            image: 'assets/images/svg/user-search.svg'
          },
          {
            title: 'Responsables RH',
            total_no: totalRH.toString(),
            image: 'assets/images/svg/briefcase.svg'
          },
          {
            title: 'Partenaires externes',
            total_no: totalExternes.toString(),
            image: 'assets/images/svg/partner.svg'
          }
        ];
      },
      error: (err) => {
        console.error('Erreur chargement statistiques utilisateurs', err);
      }
    });
  }
}
