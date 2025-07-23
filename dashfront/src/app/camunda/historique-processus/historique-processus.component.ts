import { Component, OnInit } from '@angular/core';
import { CamundaService, HistoricProcessInstance } from 'app/services/camunda.service';

@Component({
  selector: 'app-historique-processus',
  templateUrl: './historique-processus.component.html',
  styleUrls: ['./historique-processus.component.css']
})
export class HistoriqueProcessusComponent implements OnInit {
  historiques: HistoricProcessInstance[] = [];
  loading = false;
  page = 0;
  size = 10;

  constructor(private camundaService: CamundaService) {}

  ngOnInit(): void {
    this.loadHistoricProcesses();
  }

  loadHistoricProcesses(): void {
    this.loading = true;
    this.camundaService.getAllHistoricProcessInstances(this.page, this.size).subscribe({
      next: (data) => {
        this.historiques = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur chargement historique :', err);
        this.historiques = [];
        this.loading = false;
      }
    });
  }

  previousPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadHistoricProcesses();
    }
  }

  nextPage(): void {
    this.page++;
    this.loadHistoricProcesses();
  }

  getStateBadgeColor(state: string | undefined): string {
    switch ((state || '').toUpperCase()) {
      case 'COMPLETED': return 'badge bg-success';
      case 'RUNNING': return 'badge bg-warning text-dark';
      default: return 'badge bg-secondary';
    }
  }
}
