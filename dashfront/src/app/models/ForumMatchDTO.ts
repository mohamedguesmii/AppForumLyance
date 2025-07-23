export interface ForumMatchDTO {
  id: number;           // id de l'événement/forum
  titre: string;        // titre du forum
  description?: string; // optionnel, description
  adresse?: string;     // lieu
  score: number;        // score de matching
}
