import { Component, OnInit } from '@angular/core';
import { EvenementAvenirService } from 'app/services/evenement-avenir.service';
import { Evenement } from 'app/services/evenement.service';
import { timer } from 'rxjs';

@Component({
  selector: 'app-evenementsvenir',
  templateUrl: './evenementsvenir.component.html',
  styleUrls: ['./evenementsvenir.component.css']
})
export class EvenementsvenirComponent implements OnInit {
  evenements: Evenement[] = [];
  loading = false;
  errorMessage = '';
style: any;

  constructor(private evenementAvenirService: EvenementAvenirService) {}

  ngOnInit(): void {
    this.loadEvenementsScrappes();
  }

  loadEvenementsScrappes(): void {
  this.loading = true;
  this.errorMessage = '';

  this.evenementAvenirService.getEvenementsScrappesAvenir().subscribe({
    next: (data) => {
      console.log('Événements reçus:', data);  // <-- Ajoute ce log ici
      this.evenements = data;
      this.loading = false;
    },
    error: (err) => {
      this.errorMessage = 'Erreur lors du chargement des événements scrappés.';
      this.loading = false;
      console.error(err);
    }
  });
}


  relancerScraping(): void {
    this.loading = true;
    this.errorMessage = '';
    console.log('Début du scraping');

    this.evenementAvenirService.lancerScraping().subscribe({
      next: (res) => {
        console.log('Scraping réussi:', res);
        // Attendre 2 secondes avant de recharger la liste, pour laisser le temps au backend de sauvegarder
        timer(2000).subscribe(() => {
          this.loadEvenementsScrappes();
        });
      },
      error: (err) => {
        console.error('Erreur scraping:', err);
        this.errorMessage = 'Erreur lors du scraping des événements.';
        this.loading = false;
      }
    });
  }
}
