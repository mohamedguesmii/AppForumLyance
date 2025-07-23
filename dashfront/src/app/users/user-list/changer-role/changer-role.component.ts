import { Component, OnInit } from '@angular/core';
import { UserService } from 'app/services/userService/user.service';
import { User } from 'app/models/user';
import { forkJoin } from 'rxjs';
import { Location } from '@angular/common';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-changer-role',
  templateUrl: './changer-role.component.html',
  styleUrls: ['./changer-role.component.css']
})
export class ChangerRoleComponent implements OnInit {
  users: User[] = [];

  // Utilisateurs dont le rôle a été modifié (affichage du tick)
  changedUsers = new Set<number>();

  // Map userId -> nouveau rôle sélectionné
  modifiedUsers: Map<number, string> = new Map();

  constructor(private userService: UserService, private location: Location) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (data) => {
        this.users = data;
        if (this.users.length > 0) {
          console.log('Premier utilisateur:', this.users[0]);
          console.log('Roles du premier utilisateur:', this.users[0].roles);
        }
      },
      error: (err) => {
        console.error(err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors du chargement des utilisateurs',
          confirmButtonText: 'Fermer'
        });
      }
    });
  }

  getDisplayedRole(user: User): string {
    return this.modifiedUsers.get(user.id) ?? (user.role ?? '');
  }

  onRoleChange(user: User, newRole: string): void {
    if (!newRole) {
      this.modifiedUsers.delete(user.id);
      this.changedUsers.delete(user.id);
    } else {
      const currentRole = user.role ?? '';
      if (currentRole !== newRole) {
        this.modifiedUsers.set(user.id, newRole);
        this.changedUsers.add(user.id);
      } else {
        this.modifiedUsers.delete(user.id);
        this.changedUsers.delete(user.id);
      }
    }
  }

  saveChanges(): void {
    if (this.modifiedUsers.size === 0) {
      Swal.fire({
        icon: 'info',
        title: 'Aucun changement',
        text: 'Aucun changement à enregistrer.',
        timer: 2500,
        showConfirmButton: false,
        position: 'top'
      });
      return;
    }

    const updates = [];
    this.modifiedUsers.forEach((newRole, userId) => {
      updates.push(this.userService.updateRoles(userId, [newRole]));
    });

    forkJoin(updates).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: 'Les rôles ont été mis à jour avec succès.',
          timer: 3000,
          showConfirmButton: false,
          position: 'top'
        });
        this.modifiedUsers.clear();
        this.changedUsers.clear();
        this.loadUsers();
      },
      error: (err) => {
        console.error(err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: 'Erreur lors de la mise à jour des rôles.',
          confirmButtonText: 'Fermer',
          position: 'top'
        });
      }
    });
  }

  saveSingleUser(user: User): void {
    const newRole = this.modifiedUsers.get(user.id);
    if (!newRole) {
      Swal.fire({
        icon: 'info',
        title: 'Aucun changement',
        text: 'Aucun changement à enregistrer pour cet utilisateur.',
        timer: 2500,
        showConfirmButton: false,
        position: 'top'
      });
      return;
    }

    this.userService.updateRoles(user.id, [newRole]).subscribe({
      next: () => {
        Swal.fire({
          icon: 'success',
          title: 'Succès',
          text: `Rôle mis à jour avec succès pour ${user.username}.`,
          timer: 3000,
          showConfirmButton: false,
          position: 'top'
        });
        this.modifiedUsers.delete(user.id);
        this.changedUsers.delete(user.id);
        this.loadUsers();
      },
      error: (err) => {
        console.error(err);
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: `Erreur lors de la mise à jour du rôle de ${user.username}.`,
          confirmButtonText: 'Fermer',
          position: 'top'
        });
      }
    });
  }

  goBack(): void {
    this.location.back();
  }
}
