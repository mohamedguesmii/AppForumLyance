import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { CamundaService, Task, Candidature } from 'app/services/camunda.service';
import { firstValueFrom } from 'rxjs';

interface ForumHistorique {
  id: string;
  titre: string;
  statut: string;
  dateDerniereAction: string | null;
  type: 'offre' | 'candidature';
  processInstanceId: string;
}

@Component({
  selector: 'app-validations',
  templateUrl: './validations.component.html',
  styleUrls: ['./validations.component.css']
})
export class ValidationsComponent implements OnInit {

  @ViewChild('offresCanvas', { static: false }) offresCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('candidaturesCanvas', { static: false }) candidaturesCanvas!: ElementRef<HTMLCanvasElement>;

  tasks: Task[] = [];
  offres: Task[] = [];
  candidatures: Task[] = [];

  offresHistorique: ForumHistorique[] = [];
  candidaturesHistorique: ForumHistorique[] = [];

  historiqueCandidatures: Candidature[] = [];

  totalItems = 0;
  valides = 0;
  refuses = 0;
  enAttente = 0;
  percentValides = 0;
  percentRefuses = 0;
  percentEnAttente = 0;

  // Statistiques offres
  totalOffres = 0;
  offresValides = 0;
  offresRefuses = 0;
  offresEnAttente = 0;

  // Statistiques candidatures
  totalCandidatures = 0;
  candidaturesValides = 0;
  candidaturesRefuses = 0;
  candidaturesEnAttente = 0;

  private _selectedStatType: 'offres' | 'candidatures' = 'offres';

  get selectedStatType(): 'offres' | 'candidatures' {
    return this._selectedStatType;
  }

  set selectedStatType(value: 'offres' | 'candidatures') {
    this._selectedStatType = value;

    // Détruire les anciens charts avant de recréer
    this.destroyCharts();

    // Selon la sélection, recréer le chart correspondant
    if (value === 'offres') {
      this.updateOffresChart();
    } else {
      this.updateCandidaturesChart();
    }
  }

  loading = false;
  selectedFilter = 'TOUS';
  showHistoriqueSection: 'offres' | 'candidatures' = 'offres';

  selectedProcessId: string | null = null;
  selectedProcessType: 'offre' | 'candidature' | null = null;
  displayInlineDetails = false;

  task: Task | null = null;
  item: Task | ForumHistorique | null = null;

  // <-- Nouvelle propriété pour stocker les variables de la tâche
  taskVariables: Record<string, any> | null = null;

  private offresChart: Chart | null = null;
  private candidaturesChart: Chart | null = null;

  constructor(private camundaService: CamundaService, private router: Router) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    this.loadAll();
    this.loadHistoriqueCandidatures();
  }

  async loadAll(): Promise<void> {
    this.loading = true;
    try {
      const [tasks, statuses] = await Promise.all([
        firstValueFrom(this.camundaService.getTasksForRh()),
        firstValueFrom(this.camundaService.getAllStatuses())
      ]);

      this.tasks = tasks.map(t => {
        const nomFormulaire = t.variables?.nomFormulaire?.value ?? '';
        const statutValidation = t.variables?.statutValidation?.value ?? 'EN_ATTENTE';
        const name = t.name?.toLowerCase() ?? '';
        const formKey = t.formKey?.toLowerCase() ?? '';
        const typeVar = t.variables?.type?.value?.toLowerCase() ?? '';

        const typeStr = [nomFormulaire.toLowerCase(), typeVar, name, formKey].join(' ');

        let type: 'offre' | 'candidature' = 'offre';
        if (typeStr.includes('candidature') || typeStr.includes('candidate') || typeStr.includes('candidat')) {
          type = 'candidature';
        } else if (typeStr.includes('offre') || typeStr.includes('offer')) {
          type = 'offre';
        }

        return {
          ...t,
          nomFormulaire,
          statut: statutValidation,
          type
        };
      });

      this.offres = this.tasks.filter(t => t.type === 'offre');
      this.candidatures = this.tasks.filter(t => t.type === 'candidature');

      this.offresHistorique = [];
      this.candidaturesHistorique = [];

      statuses.forEach(s => {
        let typeValue: 'offre' | 'candidature' = 'offre';

        const rawType = s.type?.toLowerCase() ?? '';

        if (rawType.includes('candidature') || s.titre?.toLowerCase().includes('candidature')) {
          typeValue = 'candidature';
        } else if (rawType.includes('offre') || s.titre?.toLowerCase().includes('offre')) {
          typeValue = 'offre';
        }

        let id = '';
        if (typeValue === 'offre') id = s.offreId?.toString() ?? '';
        else if (typeValue === 'candidature') id = s.candidatureId?.toString() ?? '';

        const historique: ForumHistorique = {
          id,
          titre: s.titre || 'Sans titre',
          statut: (s.statut || 'EN_ATTENTE').toUpperCase().trim(),
          dateDerniereAction: s.dateDerniereAction ?? null,
          type: typeValue,
          processInstanceId: s.processInstanceId ?? ''
        };

        if (typeValue === 'offre') {
          this.offresHistorique.push(historique);
        } else {
          this.candidaturesHistorique.push(historique);
        }
      });

      this.updateStats([...this.offresHistorique, ...this.candidaturesHistorique]);

    } catch (error) {
      console.error('❌ Erreur lors du chargement des données Camunda :', error);
    } finally {
      this.loading = false;
    }
  }

  loadHistoriqueCandidatures(): void {
    this.camundaService.getAllCandidatures().subscribe({
      next: data => {
        this.historiqueCandidatures = data;
      },
      error: err => {
        console.error('❌ Erreur lors du chargement des candidatures :', err);
      }
    });
  }

  updateStats(all: ForumHistorique[]): void {
    this.totalItems = all.length;

    this.valides = all.filter(h => h.statut === 'VALIDÉ').length;
    this.refuses = all.filter(h => h.statut === 'REFUSÉ').length;
    this.enAttente = this.totalItems - this.valides - this.refuses;

    // Statistiques offres
    const offres = all.filter(h => h.type === 'offre');
    this.totalOffres = offres.length;
    this.offresValides = offres.filter(h => h.statut === 'VALIDÉ').length;
    this.offresRefuses = offres.filter(h => h.statut === 'REFUSÉ').length;
    this.offresEnAttente = this.totalOffres - this.offresValides - this.offresRefuses;

    // Statistiques candidatures
    const candidatures = all.filter(h => h.type === 'candidature');
    this.totalCandidatures = candidatures.length;
    this.candidaturesValides = candidatures.filter(h => h.statut === 'VALIDÉ').length;
    this.candidaturesRefuses = candidatures.filter(h => h.statut === 'REFUSÉ').length;
    this.candidaturesEnAttente = this.totalCandidatures - this.candidaturesValides - this.candidaturesRefuses;

    this.percentValides = this.calcPercent(this.valides);
    this.percentRefuses = this.calcPercent(this.refuses);
    this.percentEnAttente = this.calcPercent(this.enAttente);

    // Met à jour le graphique en fonction du type sélectionné
    if (this.selectedStatType === 'offres') {
      this.updateOffresChart();
    } else {
      this.updateCandidaturesChart();
    }
  }

  calcPercent(count: number): number {
    return this.totalItems ? Math.round((count * 100) / this.totalItems) : 0;
  }

  getBadgeClass(statut: string): string {
    switch (statut?.toUpperCase()) {
      case 'VALIDÉ': return 'badge bg-success';
      case 'REFUSÉ': return 'badge bg-danger';
      default: return 'badge bg-warning text-dark';
    }
  }

  filteredTasks(tasks: Task[]): Task[] {
    if (this.selectedFilter === 'TOUS') return tasks;
    return tasks.filter(t => (t.statut || '').toUpperCase() === this.selectedFilter);
  }

  complete(taskId: string, statut: 'VALIDÉ' | 'REFUSÉ'): void {
    this.camundaService.completeTask(taskId, statut).subscribe({
      next: () => {
        this.taskVariables = null; // Reset variables après action
        this.loadAll();
      },
      error: e => console.error('❌ Erreur lors de la complétion de tâche', e)
    });
  }

  deleteProcess(task: Task): void {
    if (!task?.processInstanceId) return;
    if (!confirm('❗ Confirmez la suppression de ce processus ?')) return;
    this.camundaService.deleteProcessInstance(task.processInstanceId).subscribe({
      next: () => this.loadAll(),
      error: e => console.error('❌ Erreur lors de la suppression', e)
    });
  }

  // --- NOUVEAU : Charger les variables d'une tâche (contraintes, données) ---
  loadTaskVariables(taskId: string): void {
    this.camundaService.getTaskVariables(taskId).subscribe({
      next: vars => {
        this.taskVariables = vars;
      },
      error: err => {
        console.error('❌ Erreur lors du chargement des variables de tâche', err);
        this.taskVariables = null;
      }
    });
  }

  goToDetails(item: Task | ForumHistorique): void {
  if (!item || !item.processInstanceId) return;

  this.displayInlineDetails = true;
  this.selectedProcessId = item.processInstanceId;

  this.selectedProcessType =
    'nomFormulaire' in item
      ? (item.nomFormulaire.toLowerCase().includes('offre') ? 'offre' : 'candidature')
      : (item as ForumHistorique).type;

  this.item = item;

  this.taskVariables = null; // reset avant chargement

  // Exemple d'appel pour récupérer variables (adapter selon ton service)
  this.camundaService.getTaskVariables(item.id).subscribe({
    next: vars => {
      this.taskVariables = vars;
    },
    error: err => {
      console.error('Erreur récupération variables tâche', err);
      this.taskVariables = null;
    }
  });
}


  closeDetails(): void {
    this.displayInlineDetails = false;
    this.selectedProcessId = null;
    this.selectedProcessType = null;
    this.item = null;
    this.taskVariables = null;
  }

  goToWorkflowMonitoring(): void {
    this.router.navigate(['/workflow-monitoring']);
  }

  goToHistorique(): void {
    this.router.navigate(['/historique-processus']);
  }

  goToProcessDetails(processInstanceId?: string): void {
    if (!processInstanceId) return; // Ne fait rien si id manquant
    this.router.navigate(['/processus-details', processInstanceId]);
  }

  trackByProcessId(index: number, item: Task | ForumHistorique): string {
    return item.processInstanceId;
  }

  // --- Gestion des charts ---

  private destroyCharts(): void {
    if (this.offresChart) {
      this.offresChart.destroy();
      this.offresChart = null;
    }
    if (this.candidaturesChart) {
      this.candidaturesChart.destroy();
      this.candidaturesChart = null;
    }
  }

  private updateOffresChart(): void {
    if (!this.offresCanvas) return;
    const ctx = this.offresCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const data = [this.offresValides, this.offresRefuses, this.offresEnAttente];

    if (this.offresChart) {
      this.offresChart.data.datasets[0].data = data;
      this.offresChart.update();
    } else {
      this.offresChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Validés', 'Refusés', 'En attente'],
          datasets: [{
            label: 'Offres',
            data: data,
            backgroundColor: ['#198754', '#dc3545', '#ffc107']
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: { beginAtZero: true, ticks: { stepSize: 1 } }
          },
          plugins: {
            legend: { display: true },
            title: { display: true, text: 'Statistiques Offres' }
          }
        }
      });
    }
  }

  private updateCandidaturesChart(): void {
    if (!this.candidaturesCanvas) return;
    const ctx = this.candidaturesCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const data = [this.candidaturesValides, this.candidaturesRefuses, this.candidaturesEnAttente];

    if (this.candidaturesChart) {
      this.candidaturesChart.data.datasets[0].data = data;
      this.candidaturesChart.update();
    } else {
      this.candidaturesChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Validés', 'Refusés', 'En attente'],
          datasets: [{
            label: 'Candidatures',
            data: data,
            backgroundColor: ['#198754', '#dc3545', '#ffc107']
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: { beginAtZero: true, ticks: { stepSize: 1 } }
          },
          plugins: {
            legend: { display: true },
            title: { display: true, text: 'Statistiques Candidatures' }
          }
        }
      });
    }
  }
}
