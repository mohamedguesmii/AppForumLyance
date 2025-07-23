import { Component, OnInit } from '@angular/core';
import { UserService } from 'app/services/userService/user.service';
import { User } from 'app/models/user';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  notificationMessage: string = '';
  isEditing: boolean = false;
  selectedUser: User | null = null;
  tokenStorage: any;
  user: any;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (data) => {
        this.users = data;
        this.notificationMessage = '';
      },
      error: (err) => {
        console.error('Erreur chargement utilisateurs:', err);
        this.notificationMessage = 'Erreur lors du chargement des utilisateurs.';
      }
    });
  }

  deleteUser(id?: number): void {
    if (!id) return;
    if (confirm("Voulez-vous vraiment supprimer cet utilisateur ?")) {
      this.userService.delete(id).subscribe({
        next: () => {
          this.notificationMessage = 'Utilisateur supprimé avec succès';
          this.loadUsers();
        },
        error: err => {
          this.notificationMessage = 'Erreur lors de la suppression.';
        }
      });
    }
  }

  editUser(user: User): void {
    this.selectedUser = { ...user };
    this.isEditing = true;
  }

 saveUser(): void {
  if (!this.selectedUser || this.selectedUser.id == null) return;

  const updatedUser = {
    username: this.selectedUser.username,
    email: this.selectedUser.email,
    password: this.selectedUser.password || '', // Assure-toi que password est défini ou passe un vide
    firstName: this.selectedUser.firstName,
    lastName: this.selectedUser.lastName,
    phoneNumber: this.selectedUser.phoneNumber,
    address: this.selectedUser.address,
    roles: this.selectedUser.roles?.map(role => role.name) ?? []
  };
console.log("Contenu envoyé à l'API :", updatedUser);
  this.userService.update(this.selectedUser.id, updatedUser).subscribe({
    next: () => {
      this.notificationMessage = 'Utilisateur modifié avec succès';
      this.isEditing = false;
      this.selectedUser = null;
      this.loadUsers();
    },
    error: err => {
      this.notificationMessage = 'Erreur lors de la modification.';
      console.error('Erreur PUT:', err);
    }
  });
}


  cancelEdit(): void {
    this.isEditing = false;
    this.selectedUser = null;
  }

  activateRole(user: User, newRole: string): void {
  if (!user || !user.id) return;

  this.userService.updateRoles(user.id, [newRole]).subscribe({
    next: (updatedUser) => {
      this.notificationMessage = `Rôle ${newRole} activé avec succès.`;
      this.loadUsers();

      // Mise à jour locale du user stocké (si l’utilisateur connecté est modifié)
      const currentUser = this.tokenStorage.getUser();
      if (currentUser && currentUser.id === updatedUser.id) {
        this.tokenStorage.saveUser(updatedUser);
      }
    },
    error: (err) => {
      this.notificationMessage = 'Erreur lors du changement de rôle.';
    }
  });
}

}
