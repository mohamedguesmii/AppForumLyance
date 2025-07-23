import { Role } from './role';

export interface User {
  name: string;
  avatarUrl: string;
  role: string;
  id?: number;  // facultatif, peut ne pas exister à la création

  firstName: string;
  lastName: string;

  username: string;
  email: string;
  phoneNumber: string;

  birthDate?: string;  // ISO 'YYYY-MM-DD'
  address?: string;

  active?: boolean;

  profilePhotoUrl?: string;
  createdAt?: string;   // ISO datetime
  lastLoginAt?: string;

  password?: string;  // généralement, ne pas exposer côté frontend

  roles?: Role[];  // plusieurs rôles possibles
}
