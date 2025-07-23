import { Component, OnInit } from '@angular/core';
import { MatchingService, ForumMatchDTO } from 'app/services/matching.service';

@Component({
  selector: 'app-matching-forums',
  templateUrl: './matching-forums.component.html',
  styleUrls: ['./matching-forums.component.css']
})
export class MatchingForumsComponent implements OnInit {

  forumsMatched: ForumMatchDTO[] = [];
  errorMsg = '';
  loading = false;
  maxScore = 10;

  // ID utilisateur fixe pour test
  testUserId = 60; 

  constructor(private matchingService: MatchingService) {}

  ngOnInit(): void {
    this.loadMatchingForums(this.testUserId);
  }

  loadMatchingForums(userId: number) {
    this.errorMsg = '';
    this.loading = true;

    this.matchingService.getMatchingForums(userId).subscribe({
      next: (data) => {
        this.forumsMatched = data;
        this.loading = false;
        if (data.length === 0) {
          this.errorMsg = 'Aucun forum recommandé pour cet utilisateur.';
        }
      },
      error: (err) => {
        this.errorMsg = 'Erreur lors du chargement des forums';
        this.loading = false;
        console.error(err);
      }
    });
  }
}
