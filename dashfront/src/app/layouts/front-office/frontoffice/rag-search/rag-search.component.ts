import { Component } from '@angular/core';
import { SearchService, Document } from 'app/services/SearchService';

interface EvenementParsed {
  titre: string;
  adresse: string;
  dateDebut: string;
  dateFin: string;
  description: string;
}

@Component({
  selector: 'app-rag-search',
  templateUrl: './rag-search.component.html',
  styleUrls: ['./rag-search.component.css']
})
export class RagSearchComponent {
  query: string = '';
  results: Document[] = [];
  loading: boolean = false;
  errorMessage: string = '';

  constructor(private searchService: SearchService) {}

  onSearch(): void {
    const trimmedQuery = this.query.trim();
    if (!trimmedQuery) {
      this.errorMessage = 'Veuillez entrer une requête.';
      this.results = [];
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.results = [];

    this.searchService.search(trimmedQuery).subscribe({
      next: (res: Document[]) => {
        // Filtrer offres et événements, trier par distance décroissante
        this.results = res
          .filter(doc => doc.type === 'offre' || doc.type === 'evenement')
          .sort((a, b) => b.distance - a.distance);

        this.loading = false;

        if (this.results.length === 0) {
          this.errorMessage = 'Aucun résultat pertinent trouvé (offres ou événements).';
        }
      },
      error: (err) => {
        console.error('Erreur API:', err);
        this.errorMessage = 'Erreur lors de la recherche.';
        this.loading = false;
      }
    });
  }

  parseEvenement(enrichedContent: string): EvenementParsed {
    // Nettoyer les caractères mal encodés
    const clean = enrichedContent.replace(/�/g, 'é');

    // Extraire champs avec des expressions régulières adaptées au format enrichi
    return {
      titre: this.extract(clean, 'Événement') || this.extract(clean, 'Titre'),
      adresse: this.extract(clean, 'Adresse'),
      dateDebut: this.extract(clean, 'Du'),
      dateFin: this.extract(clean, 'au'),
      description: this.extract(clean, 'Description')
    };
  }

  private extract(text: string, label: string): string {
    // Recherche du label suivi de ":" ou " :"
    const regex = new RegExp(`${label} ?: ?([^|\\.]+)`, 'i');
    const match = text.match(regex);
    return match ? match[1].trim() : '';
  }
}
