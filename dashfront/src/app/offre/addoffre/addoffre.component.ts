import { Component, OnInit } from '@angular/core';
import { EventService } from 'app/services/evenement.service';
import { OffreService } from 'app/services/offre.service';
import { Evenement } from 'app/models/evenement';
import { Offre } from 'app/models/offre';
import Swal from 'sweetalert2';  // <-- Import SweetAlert2

@Component({
  selector: 'app-addoffre',
  templateUrl: './addoffre.component.html',
  styleUrls: ['./addoffre.component.css']
})
export class AddoffreComponent implements OnInit {
  trackById: any;
  resetPagination() {
    throw new Error('Method not implemented.');
  }

  showForm = false;
  evenements: Evenement[] = [];
  today: string = new Date().toISOString().substring(0, 10);

  nouvelleOffre: Offre = {
    titre: '',
    description: '',
    domaine: '',
    datePublication: this.today,
    type: '',
    duree: '',
    evenementId: null,
    createurId: 1 // à adapter dynamiquement si besoin
  };

  searchTerm: any;

  constructor(
    private evenementService: EventService,
    private offreService: OffreService
  ) {}

  ngOnInit(): void {
    this.loadEvenements();
  }

  loadEvenements(): void {
    this.evenementService.getAllEvenements().subscribe({
      next: (data: Evenement[]) => {
        this.evenements = data;
        if (this.evenements.length > 0) {
          this.nouvelleOffre.evenementId = this.evenements[0].id;
        }
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des événements :', error);
      }
    });
  }

  ajouterOffre(): void {
    this.showForm = !this.showForm;
  }

  soumettreOffre(): void {
    const offreToSend: any = { ...this.nouvelleOffre };

    if (offreToSend.evenementId == null) {
      Swal.fire({
        icon: 'warning',
        title: 'Oops...',
        text: '❗ Veuillez sélectionner un événement.'
      });
      return;
    }

    // Transformation des identifiants en nombres
    offreToSend.evenementId = this.parseToNumberOrNull(offreToSend.evenementId);
    offreToSend.createurId = this.parseToNumberOrNull(offreToSend.createurId);

    // Nettoyage des champs vides
    Object.keys(offreToSend).forEach(key => {
      if (offreToSend[key] === undefined || offreToSend[key] === '') {
        console.warn(`⚠️ Champ "${key}" vide ou indéfini, supprimé.`);
        delete offreToSend[key];
      }
    });

    console.log("📤 Offre à envoyer :", offreToSend);

    this.offreService.ajouterOffre(offreToSend).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: '✅ Offre ajoutée avec succès !',
          timer: 2000,
          showConfirmButton: false
        });
        this.resetForm();
        this.showForm = false;
      },
      error: (err) => {
        console.error('❌ Erreur lors de l\'ajout de l\'offre :', err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur : ' + (err.error ? JSON.stringify(err.error) : "Vérifie les champs"),
        });
      }
    });
  }

  annuler(): void {
    this.resetForm();
    this.showForm = false;
  }

  private resetForm(): void {
    this.nouvelleOffre = {
      titre: '',
      description: '',
      domaine: '',
      datePublication: this.today,
      type: '',
      duree: '',
      evenementId: this.evenements.length > 0 ? this.evenements[0].id : null,
      createurId: 1
    };
  }

  private parseToNumberOrNull(value: any): number | null {
    const parsed = Number(value);
    return isNaN(parsed) ? null : parsed;
  }

  ouvrirFormAvecEvent(eventId: number): void {
    this.nouvelleOffre.evenementId = eventId; // sélectionne l'événement cliqué
    this.showForm = true; // affiche le formulaire
    // Optionnel : scroll vers le formulaire pour mieux voir
    setTimeout(() => {
      const formElement = document.getElementById('formAddOffre');
      if (formElement) {
        formElement.scrollIntoView({ behavior: 'smooth' });
      }
    }, 100);
  }
}
