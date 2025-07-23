import { Component, OnInit, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CamundaService, Task } from 'app/services/camunda.service';
import BpmnViewer from 'bpmn-js';
import { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-processus-details',
  templateUrl: './processus-details.component.html',
  styleUrls: ['./processus-details.component.css']
})
export class ProcessusDetailsComponent implements OnInit, AfterViewInit {
  task: Task | null = null;
  processInstanceId: string = '';
  loading = false;
  hasDiagram = false;
  etatStats = { ongoing: 0, finished: 0 };
  etatChartInstance: any;

  constructor(
    private route: ActivatedRoute,
    private camundaService: CamundaService
  ) {}

  ngOnInit(): void {
    this.processInstanceId = this.route.snapshot.paramMap.get('id') || '';
    if (this.processInstanceId) {
      this.loadTask();
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.task) {
        this.displayChart();
      }
    }, 300);
  }

  loadTask(): void {
    this.loading = true;
    this.camundaService.getTasksForRh().subscribe({
      next: (tasks) => {
        this.task = tasks.find(t => t.processInstanceId === this.processInstanceId) || null;
        this.loading = false;

        if (this.task) {
          this.etatStats = {
            ongoing: this.task.statut === 'VALIDÉ' || this.task.statut === 'REFUSÉ' ? 0 : 1,
            finished: this.task.statut === 'VALIDÉ' || this.task.statut === 'REFUSÉ' ? 1 : 0
          };
          this.loadDiagram();
        }
      },
      error: (err) => {
        console.error('Erreur chargement tâche RH :', err);
        this.loading = false;
      }
    });
  }

  getStatutColor(statut: string | undefined): string {
    switch ((statut || '').toUpperCase()) {
      case 'VALIDÉ': return 'text-success';
      case 'REFUSÉ': return 'text-danger';
      default: return 'text-warning';
    }
  }

  loadDiagram(): void {
    const container = document.getElementById('bpmnCanvas');
    if (!container || !this.task?.processInstanceId) return;

    const viewer = new BpmnViewer({ container });

    // Récupérer le diagramme via un ID fictif ou depuis ton backend si disponible
    this.camundaService.getHistoricProcessInstanceById(this.task.processInstanceId).subscribe({
      next: (instance) => {
        const definitionId = instance.processDefinitionId;
        // 💡 Ici tu dois avoir un autre endpoint type `getBpmnXml(defId)` qui retourne le XML
        this.camundaService.getBpmnXml(definitionId).subscribe({
          next: (res: any) => {
            if (res?.bpmn20Xml?.includes('<bpmn:process')) {
              this.hasDiagram = true;
              viewer.importXML(res.bpmn20Xml, (err: any) => {
                if (err) {
                  console.error('Erreur import diagramme :', err);
                }
              });
            }
          },
          error: () => this.hasDiagram = false
        });
      },
      error: () => this.hasDiagram = false
    });
  }

  displayChart(): void {
    const canvas = document.getElementById('etatChart') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (this.etatChartInstance) this.etatChartInstance.destroy();

    this.etatChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['En cours', 'Terminés'],
        datasets: [{
          data: [this.etatStats.ongoing, this.etatStats.finished],
          backgroundColor: ['#f6c23e', '#1cc88a']
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    });
  }
}
