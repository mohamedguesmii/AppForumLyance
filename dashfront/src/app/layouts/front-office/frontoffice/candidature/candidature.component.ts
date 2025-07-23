import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

interface Candidat {
  id: number;
  nom: string;
  prenom: string;
  email: string;
}

interface Candidature {
  offreId: number;
  telephone: string;
  cvFile: File | null;
  lettreMotivation: string;
}

@Component({
  selector: 'app-candidature',
  templateUrl: './candidature.component.html',
  styleUrls: ['./candidature.component.css']
})
export class CandidatureComponent implements OnInit {

  offreId: number = 0;

  candidature: Candidature = {
    offreId: 0,
    telephone: '',
    cvFile: null,
    lettreMotivation: ''
  };

  candidatConnecte?: Candidat;  // Par ex. récupérer de ton service utilisateur
loading: any;
totalPages: any;
pagedCandidatures: any;
filteredCandidatures: any;
errorMsg: any;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) {}

   navigateToHome(): void {
    this.router.navigate(['/home']); // adapte la route selon ton routing
  }
  ngOnInit(): void {
    // Récupérer l'id de l'offre depuis la query param
    this.route.queryParams.subscribe(params => {
      this.offreId = +params['offreId'] || 0;
      this.candidature.offreId = this.offreId;
      console.log('Offre à candidater:', this.offreId);
    });

    // Simuler candidat connecté (normalement récupérer depuis un service)
    this.candidatConnecte = {
      id: 1,
      nom: 'Dupont',
      prenom: 'Jean',
      email: 'jean.dupont@example.com'
    };
  }

  // Pour récupérer le fichier CV uploadé
  onFileChange(event: any) {
    if (event.target.files.length > 0) {
      this.candidature.cvFile = event.target.files[0];
    }
  }

  onSubmit() {
    // Validation simple
    if (!this.candidature.telephone) {
      alert('Le téléphone est obligatoire.');
      return;
    }
    if (!this.candidature.cvFile) {
      alert('Le CV est obligatoire.');
      return;
    }

    // Afficher les infos pour vérification
    console.log('Candidature envoyée:', this.candidature);
    alert(`Candidature envoyée pour l'offre #${this.candidature.offreId} par ${this.candidatConnecte?.prenom} ${this.candidatConnecte?.nom}`);

    // TODO : Appel au service backend pour envoyer la candidature

    // Réinitialiser le formulaire
    this.candidature.telephone = '';
    this.candidature.cvFile = null;
    this.candidature.lettreMotivation = '';
  }
}
