import { Component, OnInit } from '@angular/core';
import { CamundaService, Status } from 'app/services/camunda.service';

interface Processus {
  id: string;
  titre: string;
  statut: string;
  type: 'offre' | 'candidature' | 'forum';
  processInstanceId: string;
}

@Component({
  selector: 'app-workflow-monitoring',
  templateUrl: './workflow-monitoring.component.html',
  styleUrls: ['./workflow-monitoring.component.css']
})
export class WorkflowMonitoringComponent implements OnInit {

  processusList: Processus[] = [];
  loading = false;

  constructor(private camundaService: CamundaService) {}

  ngOnInit(): void {
    this.loadProcessus();
  }

  loadProcessus(): void {
    this.loading = true;
    this.camundaService.getAllStatuses().subscribe({
      next: (data) => {
        this.processusList = (data || []).map(p => {
          const validTypes = ['offre', 'candidature', 'forum'] as const;
          const typeValue = validTypes.includes(p.type as any) ? (p.type as 'offre' | 'candidature' | 'forum') : 'offre';

          // Utilisation de l'ID correct selon le type (forumId, offreId, candidatureId)
          let id = '';
          if (typeValue === 'forum') id = p.forumId?.toString() ?? '';
          else if (typeValue === 'offre') id = p.offreId?.toString() ?? '';
          else if (typeValue === 'candidature') id = p.candidatureId?.toString() ?? '';

          return {
            id,
            titre: p.titre,
            statut: p.statut,
            type: typeValue,
            processInstanceId: p.processInstanceId
          };
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur chargement processus :', err);
        this.processusList = [];
        this.loading = false;
      }
    });
  }

  getBadgeClass(statut: string | undefined): string {
    switch ((statut || '').toUpperCase()) {
      case 'VALIDÉ': return 'bg-success';
      case 'REFUSÉ': return 'bg-danger';
      default: return 'bg-warning text-dark';
    }
  }
}
