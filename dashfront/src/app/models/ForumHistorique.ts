export interface ForumHistorique {
  id: string;
  titre: string;
  statut: string;
  dateDerniereAction: string | null;
  type: 'offre' | 'candidature' | 'forum';
  processInstanceId: string;
}
