import { HttpEventType } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { Evenement } from 'app/models/evenement';
import { EventService } from 'app/services/evenement.service';
import { ReservationService } from 'app/services/reservation.service';
import { Router } from '@angular/router';
import { debounceTime } from 'rxjs/operators';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-evenement',
  templateUrl: './evenement.component.html',
  styleUrls: ['./evenement.component.css']
})
export class EvenementComponent implements OnInit {
  evenements: Evenement[] = [];
  filteredEvenements: Evenement[] = [];
  paginatedEvenements: Evenement[] = [];

  evenementForm: FormGroup;
  showNotification = false;
  notificationMessage = '';

  isEditing: boolean = false;
  selectedEvenement: Evenement | null = null;

  isAddModalOpen = false;
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;

  currentUserId: number = 1;

  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;

  // Recherche
  searchControl = new FormControl('');

  // Sélection multiple
  selectedEvenements: number[] = [];

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private eventService: EventService,
    private reservationService: ReservationService
  ) {
    this.evenementForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      capacity: [null, [Validators.required, Validators.min(1)]],
      datedebut: ['', Validators.required],
      datefin: ['', Validators.required],
      adresse: ['', [Validators.required, Validators.pattern('[A-Za-z0-9\\s]{5,}')]],
      image: [null]
    });
  }

  ngOnInit(): void {
    this.loadEvenements();

    this.searchControl.valueChanges
      .pipe(debounceTime(300))
      .subscribe(search => this.filterEvenements(search));
  }

  loadEvenements(): void {
    this.eventService.getAllEvenements().subscribe({
      next: (data) => {
        this.evenements = data ?? [];
        this.filterEvenements();
      },
      error: (err) => {
        console.error(err);
        this.evenements = [];
        this.filterEvenements();
      }
    });
  }

  filterEvenements(search: string = '') {
    const query = search.toLowerCase();
    this.filteredEvenements = (this.evenements ?? []).filter(ev =>
      ev.title.toLowerCase().includes(query)
    );

    this.totalPages = Math.ceil(this.filteredEvenements.length / this.itemsPerPage);
    this.currentPage = 1;
    this.updatePagination();
  }

  updatePagination() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    this.paginatedEvenements = this.filteredEvenements.slice(start, start + this.itemsPerPage);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
  }

  toggleSelectAll(event: any) {
    const checked = event.target.checked;
    if (checked) {
      this.selectedEvenements = this.paginatedEvenements.map(ev => ev.idevent);
    } else {
      this.selectedEvenements = [];
    }
  }

  toggleSelection(id: number) {
    if (this.selectedEvenements.includes(id)) {
      this.selectedEvenements = this.selectedEvenements.filter(eid => eid !== id);
    } else {
      this.selectedEvenements.push(id);
    }
  }

  deleteSelected(): void {
    if (!confirm('Voulez-vous vraiment supprimer ces événements ?')) return;

    this.selectedEvenements.forEach(id => this.deleteEvenement(id));
    this.selectedEvenements = [];
  }

  openAddModal(): void {
    this.isAddModalOpen = true;
  }

  closeAddModal(): void {
    this.isAddModalOpen = false;
    this.evenementForm.reset();
    this.selectedFile = null;
    this.imagePreview = null;
    this.isEditing = false;
  }

  onFileSelected(event: any): void {
    if (event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];

      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  addEvenement(): void {
    if (this.evenementForm.invalid) return;

    const datedebut = new Date(this.evenementForm.get('datedebut')?.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (datedebut <= today) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: "La date de début doit être strictement après aujourd'hui.",
      });
      return;
    }

    const datefin = new Date(datedebut);
    datefin.setDate(datefin.getDate() + 6);

    const overlap = (this.evenements ?? []).some(ev => {
      const evStart = new Date(ev.datedebut);
      const evEnd = new Date(ev.datefin);
      return (datedebut <= evEnd && datefin >= evStart);
    });

    if (overlap) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: "Il existe déjà un événement dans cette période.",
      });
      return;
    }

    this.evenementForm.patchValue({
      datefin: datefin.toISOString().split('T')[0]
    });

    const formData = this.buildFormData();

    this.eventService.addEvenement(formData).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.Response) {
          const newEvent: Evenement = event.body!;
          this.evenements.push(newEvent);
          this.filterEvenements(this.searchControl.value || '');
          Swal.fire({
            icon: 'success',
            title: 'Succès',
            text: 'Événement ajouté avec succès !',
            timer: 2000,
            showConfirmButton: false
          });
          this.closeAddModal();
        }
      },
      error: (err) => {
        console.error("Erreur ajout événement :", err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de l\'ajout de l\'événement.',
        });
      }
    });
  }

  deleteEvenement(id: number): void {
    this.eventService.deleteEvenement(id).subscribe({
      next: () => {
        this.evenements = this.evenements.filter(e => e.idevent !== id);
        this.filterEvenements(this.searchControl.value || '');
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: 'Événement supprimé avec succès !',
          timer: 2000,
          showConfirmButton: false
        });
      },
      error: (err) => {
        console.error(err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de la suppression de l\'événement.',
        });
      }
    });
  }

  selectEvenement(evenement: Evenement): void {
    this.selectedEvenement = evenement;
    this.isEditing = true;
    this.openAddModal();

    this.evenementForm.patchValue({
      title: evenement.title,
      description: evenement.description,
      capacity: evenement.capacity,
      datedebut: evenement.datedebut,
      datefin: evenement.datefin,
      adresse: evenement.adresse
    });

    this.imagePreview = evenement.image;

    setTimeout(() => {
      const modalElement = document.querySelector('.modal-dialog');
      if (modalElement) {
        modalElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  updateEvenement(): void {
    if (!this.selectedEvenement || this.evenementForm.invalid) return;

    const datedebut = new Date(this.evenementForm.get('datedebut')?.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (datedebut <= today) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: "La date de début doit être strictement après aujourd'hui.",
      });
      return;
    }

    const datefin = new Date(datedebut);
    datefin.setDate(datefin.getDate() + 6);

    const overlap = this.evenements.some(ev => {
      if (ev.idevent === this.selectedEvenement!.idevent) return false;
      const evStart = new Date(ev.datedebut);
      const evEnd = new Date(ev.datefin);
      return (datedebut <= evEnd && datefin >= evStart);
    });
    if (overlap) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: "Il existe déjà un événement dans cette période.",
      });
      return;
    }

    this.evenementForm.patchValue({
      datefin: datefin.toISOString().split('T')[0]
    });

    const formData = this.buildFormData();

    this.eventService.updateEvenement(this.selectedEvenement.idevent!, formData).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.Response) {
          this.loadEvenements();
          Swal.fire({
            icon: 'success',
            title: 'Succès',
            text: "Événement mis à jour !",
            timer: 2000,
            showConfirmButton: false
          });
          this.closeAddModal();
        }
      },
      error: err => {
        console.error("Erreur lors de la mise à jour :", err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de la mise à jour de l\'événement.',
        });
      }
    });
  }

  updateRating(eventId: number, rating: number) {
    const evenement = this.evenements.find(e => e.idevent === eventId);
    if (evenement) {
      evenement.starRating = rating;

      this.eventService.updateRating(eventId, rating).subscribe({
        next: () => this.notificationMessage = `Note mise à jour à ${rating} étoiles.`,
        error: () => this.notificationMessage = `Erreur lors de la mise à jour de la note.`
      });
    }
  }

  buildFormData(): FormData {
    const formData = new FormData();
    formData.append('title', this.evenementForm.get('title')?.value);
    formData.append('description', this.evenementForm.get('description')?.value);
    formData.append('capacity', this.evenementForm.get('capacity')?.value.toString());
    formData.append('datedebut', this.evenementForm.get('datedebut')?.value);
    formData.append('datefin', this.evenementForm.get('datefin')?.value);
    formData.append('adresse', this.evenementForm.get('adresse')?.value);
    if (this.selectedFile) {
      formData.append('fileImage', this.selectedFile);
    }
    return formData;
  }

  showSuccess(message: string) {
    this.notificationMessage = message;
    this.showNotification = true;
    setTimeout(() => this.showNotification = false, 3000);
  }

  actualiserEvenementsExistants(): void {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const evenementsTries = [...this.evenements].sort((a, b) =>
      new Date(a.datedebut).getTime() - new Date(b.datedebut).getTime()
    );

    let dernierDateFin = new Date(today);

    evenementsTries.forEach(ev => {
      let datedebut = new Date(ev.datedebut);
      let datefin = new Date(ev.datefin);

      if (datedebut <= today || datedebut <= dernierDateFin) {
        datedebut = new Date(dernierDateFin);
        datedebut.setDate(datedebut.getDate() + 1);
      }

      datefin = new Date(datedebut);
      datefin.setDate(datefin.getDate() + 6);

      ev.datedebut = datedebut;
      ev.datefin = datefin;

      if (dernierDateFin < datefin) {
        dernierDateFin = new Date(datefin);
      }

      const formData = new FormData();
      formData.append('title', ev.title);
      formData.append('description', ev.description);
      formData.append('capacity', ev.capacity.toString());
      formData.append('datedebut', datedebut.toISOString().split('T')[0]);
      formData.append('datefin', datefin.toISOString().split('T')[0]);
      formData.append('adresse', ev.adresse);

      this.eventService.updateEvenement(ev.idevent!, formData).subscribe({
        next: () => console.log(`Événement ${ev.idevent} mis à jour : début ${formData.get('datedebut')}, fin ${formData.get('datefin')}`),
        error: err => console.error(`Erreur mise à jour événement ${ev.idevent}:`, err)
      });
    });
  }
}
