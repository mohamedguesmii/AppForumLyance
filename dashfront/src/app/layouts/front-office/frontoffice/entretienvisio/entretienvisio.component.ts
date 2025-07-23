import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { interval, Subscription } from 'rxjs';
import { Location } from '@angular/common';

interface RendezVousDTO {
  candidature: any;
  dateHeure: string;
  type: string;
  lienVisio?: string;
  candidaturePrenom: string;
  candidatureNom: string;
  candidatureEmail?: string;
  candidatureTel?: string;
  candidatureImageUrl?: string;
}

@Component({
  selector: 'app-entretienvisio',
  templateUrl: './entretienvisio.component.html',
  styleUrls: ['./entretienvisio.component.css']
})
export class EntretienvisioComponent implements OnInit, OnDestroy {

  rdv: RendezVousDTO | null = null;
  sanitizedLienVisio: SafeResourceUrl | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  rdvStatus: 'À venir' | 'En cours' | 'Terminé' = 'À venir';
  countdown: string = '';
  private countdownSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.isLoading = true;

    const token = this.route.snapshot.paramMap.get('token');
    if (token) {
      this.http.get<RendezVousDTO>(`http://localhost:8089/api/rendezvous/${token}`).subscribe({
        next: (data) => {
          this.rdv = data;

          this.rdv.candidatureEmail = this.rdv.candidatureEmail || '';
          this.rdv.candidatureTel = this.rdv.candidatureTel || '';
          this.rdv.candidatureImageUrl = this.rdv.candidatureImageUrl || '';

          if (!this.rdv.lienVisio || this.rdv.lienVisio.trim() === '') {
            this.rdv.lienVisio = this.generateJitsiLink(token, this.rdv.candidaturePrenom, this.rdv.candidatureNom);
          }

          this.sanitizedLienVisio = this.sanitizer.bypassSecurityTrustResourceUrl(this.rdv.lienVisio);

          this.isLoading = false;
          this.startCountdown();

          // Lancer automatiquement le salon si l’entretien est en cours
          setTimeout(() => {
            if (this.rdvStatus === 'En cours') {
              this.lancerSalon();
            }
          }, 1000);
        },
        error: (err) => {
          this.errorMessage = 'Impossible de charger le rendez-vous : ' + err.message;
          this.isLoading = false;
        }
      });
    } else {
      this.errorMessage = 'Token manquant dans l\'URL.';
      this.isLoading = false;
    }
  }

  ngOnDestroy(): void {
    this.countdownSub?.unsubscribe();
  }

  goBack(): void {
    this.location.back();
  }

  private generateJitsiLink(token: string, prenom: string, nom: string): string {
    const cleanPrenom = prenom ? prenom.trim() : 'invite';
    const cleanNom = nom ? nom.trim() : 'invite';

    const roomName = `entretien-${cleanNom.toLowerCase().replace(/\s+/g, '')}-${cleanPrenom.toLowerCase().replace(/\s+/g, '')}-${token}`;
    const displayName = encodeURIComponent(`${cleanPrenom} ${cleanNom}`);

    return `https://meet.jit.si/${roomName}#userInfo.displayName="${displayName}"`;
  }

  private startCountdown() {
    this.updateStatusAndCountdown();

    this.countdownSub = interval(1000).subscribe(() => {
      this.updateStatusAndCountdown();
    });
  }

  private updateStatusAndCountdown() {
    if (!this.rdv || !this.rdv.dateHeure) return;

    const now = new Date();
    const start = new Date(this.rdv.dateHeure);
    const diff = start.getTime() - now.getTime();

    // ✅ Autoriser l'accès 30 minutes avant le début
    if (diff > 30 * 60 * 1000) { // plus de 30 min avant
      this.rdvStatus = 'À venir';
      this.countdown = this.formatDuration(diff);
    } else if (diff > -60 * 60 * 1000) { // de 30 min avant à +1h après
      this.rdvStatus = 'En cours';
      this.countdown = 'En cours';
    } else {
      this.rdvStatus = 'Terminé';
      this.countdown = '';
      this.countdownSub?.unsubscribe();
    }
  }

  private formatDuration(ms: number): string {
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${hours > 0 ? hours + 'h ' : ''}${minutes}m ${seconds}s`;
  }

  lancerSalon(): void {
    if (this.rdv?.lienVisio) {
      this.sanitizedLienVisio = this.sanitizer.bypassSecurityTrustResourceUrl(this.rdv.lienVisio);
    } else {
      alert("Le lien de la visioconférence est introuvable.");
    }
  }
}
