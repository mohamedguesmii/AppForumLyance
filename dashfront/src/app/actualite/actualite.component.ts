import { Component, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { ActualiteService } from 'app/services/actualiteService/actualite.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-actualite',
  templateUrl: './actualite.component.html',
  styleUrls: ['./actualite.component.css']
})
export class ActualiteComponent implements OnInit {
  file: File | null = null;
  actuality: string = '';
  description: string = '';
  actualites: any[] = [];

  showForm: boolean = false;      // Toggle formulaire
  isModalOpen: boolean = false;   // Modale

  constructor(private actualteService: ActualiteService, private router: Router) { }

  ngOnInit(): void {
    this.loadActualites();
  }

  loadActualites(): void {
    this.actualteService.getall().subscribe(
      (result) => {
        this.actualites = result.map(act => {
          const likesCount = act.reacts?.filter(r => r.reactionType === true).length || 0;
          const dislikesCount = act.reacts?.filter(r => r.reactionType === false).length || 0;
          return { ...act, likesCount, dislikesCount };
        });
      },
      (error) => console.error(error)
    );
  }

  fileUploaded(event: any): void {
    this.file = event.target.files[0];
    console.log('Fichier sélectionné : ', this.file);
  }

  onSubmit(): void {
    if (!this.file) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: 'Aucun fichier sélectionné'
      });
      return;
    }

    if (!this.actuality.trim() || !this.description.trim()) {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: 'Veuillez remplir tous les champs'
      });
      return;
    }

    this.actualteService.addData(this.file).subscribe(
      (result) => {
        const imageUrl = result.url;

        const formData = new FormData();
        formData.append('file', this.file); // Optionnel selon backend
        formData.append('actuality', this.actuality);
        formData.append('description', this.description);
        formData.append('url', imageUrl);

        this.actualteService.addWithDescription(formData).subscribe(
          () => {
            this.loadActualites();
            Swal.fire({
              icon: 'success',
              title: 'Succès',
              text: 'Actualité ajoutée avec succès',
              timer: 2000,
              showConfirmButton: false
            });
            this.resetForm();
            this.showForm = false; // Fermer formulaire
          },
          (error) => {
            console.error(error);
            Swal.fire({
              icon: 'error',
              title: 'Erreur',
              text: 'Erreur lors de l\'ajout'
            });
          }
        );
      },
      (error) => {
        console.error(error);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de l\'upload de l\'image'
        });
      }
    );
  }

  deleteActualite(id: number): void {
    this.actualteService.delete(id).subscribe(
      () => {
        this.loadActualites();
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: 'Actualité supprimée',
          timer: 1500,
          showConfirmButton: false
        });
      },
      (error) => {
        console.error(error);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de la suppression'
        });
      }
    );
  }

  update(id: number): void {
    this.router.navigate([`/update/${id}`]);
  }

  private resetForm(): void {
    this.actuality = '';
    this.description = '';
    this.file = null;
  }

  openModal(): void {
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  toggleForm(): void {
    this.showForm = !this.showForm;
  }
}
