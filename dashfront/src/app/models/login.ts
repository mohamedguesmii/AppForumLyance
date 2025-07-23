export interface LoginResponse {
  token: string;        // Le token JWT reçu
  type: string;         // Type de token (ex: "Bearer")
  id: number;           // ID de l'utilisateur
  username: string;     // Nom d'utilisateur
  email: string;        // Email de l'utilisateur
  roles: string[];      // Liste des rôles (ex: ["ROLE_ADMIN"])
}
