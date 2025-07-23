interface Task {
  id: string;
  name: string;
  eventTitle: string;
  statut: string;
  processInstanceId?: string;
  variables?: any;
  assignee?: string;

  // 👇 Ajoute ceci si les tâches peuvent être des 'offre' ou 'candidature'
  nomFormulaire?: 'offre' | 'candidature';
}
