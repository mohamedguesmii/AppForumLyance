import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Reservation } from 'app/models/reservation';
import { ReservationService } from 'app/services/reservation.service';
import { Chart, ChartConfiguration } from 'chart.js';
import { User } from 'app/models/user';

@Component({
  selector: 'app-reservation',
  templateUrl: './reservation.component.html',
  styleUrls: ['./reservation.component.css']
})
export class ReservationComponent implements OnInit {
  showForm: boolean = false;


  reservations: Reservation[] = [];
  selectedReservation: Reservation | null = null;
  reservationForm: FormGroup;
  reservationStats: { [key: string]: number } = {};
  private chartInstance: Chart<'pie', number[], string> | null = null;

  @ViewChild('chartCanvas', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;

  // Mapping dynamique des couleurs par titre d'événement
  eventColors: { [title: string]: string } = {};

  // Palette de couleurs par défaut
  private defaultColors = [
    '#3498db', // bleu
    '#2ecc71', // vert
    '#e67e22', // orange
    '#9b59b6', // violet
    '#e74c3c', // rouge
    '#1abc9c', // turquoise
    '#f1c40f', // jaune
    '#34495e', // gris foncé
  ];

  isModified: any;
notificationMessage: any;
isEditing: any;

  constructor(
    private reservationService: ReservationService,
    private fb: FormBuilder
  ) {
    this.reservationForm = this.fb.group({
      idreserv: [''],
      nomreserv: ['', Validators.required],  // username affiché
      nbrplace: [1, [Validators.required, Validators.min(1)]],
      type: ['', Validators.required],
      description: ['', Validators.required],
      datereserv: ['', Validators.required],

      username: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.reservationService.getAllReservations().subscribe((reservations: Reservation[]) => {
      this.reservations = reservations;
      this.generateEventColors();
      this.calculateStatistics();
    });
  }

  // Génère dynamiquement un mapping titre d'événement -> couleur
  generateEventColors(): void {
    this.eventColors = {}; // reset

    // Récupérer tous les titres uniques d'événements non vides
    const uniqueEvents = Array.from(
      new Set(
        this.reservations
          .map(r => r.evenement?.titre)
          .filter(title => !!title)
      )
    );

    uniqueEvents.forEach((eventTitle, index) => {
      this.eventColors[eventTitle!] = this.defaultColors[index % this.defaultColors.length];
    });
  }

  calculateStatistics(): void {
    this.reservationStats = {};
    this.reservations.forEach(reservation => {
      if (reservation.type) {
        this.reservationStats[reservation.type] = (this.reservationStats[reservation.type] || 0) + 1;
      }
    });
    this.renderChart();
  }

  renderChart(): void {
    if (!this.chartCanvas) return;

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    if (this.chartInstance) {
      this.chartInstance.destroy();
    }

    const labels = Object.keys(this.reservationStats);
    const data = Object.values(this.reservationStats);

    const config: ChartConfiguration<'pie'> = {
      type: 'pie',
      data: {
        labels,
        datasets: [{
          label: 'Réservations par type',
          data,
          backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'],
          hoverOffset: 30
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'top' }
        }
      }
    };

    this.chartInstance = new Chart(ctx, config as any);
  }

  selectReservation(reservation: Reservation): void {
    this.selectedReservation = { ...reservation };
    this.reservationForm.patchValue({
      idreserv: reservation.idreserv,
      nomreserv: reservation.appuser?.username || '',
      nbrplace: reservation.nbrPlaces,
      type: reservation.type,
      description: reservation.description,
      datereserv: reservation.datereserv ? reservation.datereserv.toString().substring(0, 10) : '',

      username: reservation.appuser?.username || '',
      firstName: reservation.appuser?.firstName || '',
      lastName: reservation.appuser?.lastName || '',
      email: reservation.appuser?.email || ''
    });
  }

  addReservation(): void {
    if (this.reservationForm.valid) {
      const formValue = this.reservationForm.value;

      const newUser: User = {
        id: 0,
        username: formValue.username,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        phoneNumber: '',
        active: true,
        address: '',
        password: '',
        roles: [],
        profilePhotoUrl: '',
        birthDate: undefined,
        createdAt: undefined,
        lastLoginAt: undefined,
        name: '',
        avatarUrl: '',
        role: ''
      };

      const newReservation: Reservation = {
        idreserv: 0,
        evenement: {
          idevent: 0,
          titre: '',
          title: '',
          id: 0,
          date: '',
          image: '',
          body: undefined,
          name: undefined,
          comments: undefined,
          likes: undefined,
          description: '',
          capacity: 0,
          status: '',
          datedebut: undefined,
          datefin: undefined,
          adresse: '',
          imageUrl: undefined,
          starRating: 0
        },
        appuser: newUser,
        datereserv: formValue.datereserv,
        statut: 'Inscrit',
        nbrPlaces: formValue.nbrplace,
        nomreserv: undefined,
        type: formValue.type,
        description: formValue.description,
        user: undefined
      };

      this.reservationService.addReservation(newReservation).subscribe(() => {
        this.loadReservations();
        this.resetForm();
      });
    } else {
      this.reservationForm.markAllAsTouched();
    }
  }

  updateReservation(): void {
    if (this.selectedReservation && this.reservationForm.valid) {
      const formValue = this.reservationForm.value;

      const updatedUser: User = {
        id: this.selectedReservation.appuser?.id ?? 0,
        username: formValue.username,
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        phoneNumber: this.selectedReservation.appuser?.phoneNumber || '',
        active: this.selectedReservation.appuser?.active ?? true,
        address: this.selectedReservation.appuser?.address || '',
        password: this.selectedReservation.appuser?.password || '',
        roles: Array.isArray(this.selectedReservation.appuser?.roles) ? this.selectedReservation.appuser.roles : [],
        profilePhotoUrl: this.selectedReservation.appuser?.profilePhotoUrl || '',
        birthDate: this.selectedReservation.appuser?.birthDate,
        createdAt: this.selectedReservation.appuser?.createdAt,
        lastLoginAt: this.selectedReservation.appuser?.lastLoginAt,
        name: '',
        avatarUrl: '',
        role: ''
      };

      this.selectedReservation.nomreserv = formValue.username;
      this.selectedReservation.nbrPlaces = formValue.nbrplace;
      this.selectedReservation.type = formValue.type;
      this.selectedReservation.description = formValue.description;
      this.selectedReservation.datereserv = formValue.datereserv;
      this.selectedReservation.appuser = updatedUser;

      this.reservationService.updateReservation(this.selectedReservation).subscribe(() => {
        this.loadReservations();
        this.selectedReservation = null;
        this.resetForm();
      });
    } else {
      this.reservationForm.markAllAsTouched();
    }
  }

  deleteReservation(idReservation: number): void {
    this.reservationService.deleteReservation(idReservation).subscribe(() => {
      this.loadReservations();
      this.selectedReservation = null;
    });
  }

  resetForm(): void {
    this.selectedReservation = null;
    this.reservationForm.reset({
      idreserv: '',
      nomreserv: '',
      nbrplace: 1,
      type: '',
      description: '',
      datereserv: '',
      username: '',
      firstName: '',
      lastName: '',
      email: ''
    });
  }

  // Utilitaire pour déterminer si une couleur est sombre (pour texte clair)
  isDarkColor(color: string | undefined): boolean {
    if (!color) return false;
    const c = color.charAt(0) === '#' ? color.substring(1, 7) : color;
    const r = parseInt(c.substring(0, 2), 16);
    const g = parseInt(c.substring(2, 4), 16);
    const b = parseInt(c.substring(4, 6), 16);
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;
    return brightness < 125;
  }

}
