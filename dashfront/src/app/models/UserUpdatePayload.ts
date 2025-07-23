export interface UserUpdatePayload {
  id?: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phoneNumber: string;
  birthDate?: string;
  address?: string;
  active?: boolean;
  profilePhotoUrl?: string;
  createdAt?: string;
  lastLoginAt?: string;
  password?: string;
  roles: string[];  // ici, tableau de noms de rôles
}
