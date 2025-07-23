import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ReservationService } from 'app/services/reservation.service';
import { Reservation } from 'app/models/reservation';
import { Chart, ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-reservation-stats',
  templateUrl: './reservation-stats.component.html',
  styleUrls: ['./reservation-stats.component.css']
})
export class ReservationStatsComponent implements OnInit {

  reservations: Reservation[] = [];

  // Stats globales par type
  reservationStats: { [type: string]: number } = {};
  
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;
  chartInstance: Chart<'pie', number[], string> | null = null;

  // Stats par événement et rôle
  eventRoleStats: {
    eventName: string;
    roles: { [role: string]: number };
    chartId: string;
    chartInstance?: Chart;
  }[] = [];

  constructor(private reservationService: ReservationService) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.reservationService.getAllReservations().subscribe((reservations: Reservation[]) => {
      this.reservations = reservations;

      this.calculateGlobalStats();
      setTimeout(() => this.renderGlobalChart(), 100);

      this.calculateEventRoleStats();

      setTimeout(() => {
        this.renderAllEventRoleCharts();
      }, 300);
    });
  }

  calculateGlobalStats(): void {
    this.reservationStats = {};
    this.reservations.forEach(res => {
      if (res.type) {
        this.reservationStats[res.type] = (this.reservationStats[res.type] || 0) + 1;
      }
    });
  }

  renderGlobalChart(): void {
    const ctx = this.chartCanvas?.nativeElement.getContext('2d');
    if (!ctx) {
      console.error('Contexte global chartCanvas non trouvé');
      return;
    }

    if (this.chartInstance) this.chartInstance.destroy();

    const labels = Object.keys(this.reservationStats);
    const data = Object.values(this.reservationStats);
    const backgroundColors = this.getColorSet(labels);

    const config: ChartConfiguration<'pie', number[], string> = {
      type: 'pie',
      data: {
        labels,
        datasets: [{
          label: 'Réservations par type',
          data,
          backgroundColor: backgroundColors
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'top' },
          title: { display: true, text: 'Réservations par type' }
        }
      }
    };

    this.chartInstance = new Chart(ctx, config);
  }

  calculateEventRoleStats(): void {
    const stats: { [eventName: string]: { [role: string]: number } } = {};

    this.reservations.forEach(res => {
      let rawRole = res.appuser?.role || 'Inconnu';
      if (typeof rawRole === 'string') {
        rawRole = rawRole.trim() || 'Inconnu';
      } else {
        rawRole = 'Inconnu';
      }

      const event = res.evenement?.titre || res.evenement?.title || 'Inconnu';

      if (!stats[event]) stats[event] = {};
      stats[event][rawRole] = (stats[event][rawRole] || 0) + 1;
    });

    this.eventRoleStats = Object.entries(stats).map(([eventName, roles], i) => ({
      eventName,
      roles,
      chartId: `eventRoleChart${i}`
    }));
  }

  renderAllEventRoleCharts(): void {
    this.eventRoleStats.forEach(stat => {
      const canvas = document.getElementById(stat.chartId) as HTMLCanvasElement;
      if (!canvas) return;

      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      const labels = Object.keys(stat.roles);
      const data = labels.map(l => stat.roles[l]);
      const backgroundColor = this.getColorSet(labels);

      const config: ChartConfiguration<'bar', number[], string> = {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: 'Participants par rôle',
            data,
            backgroundColor
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { display: false },
            title: {
              display: true,
              text: 'Participants par rôle',
              font: { size: 14 }
            }
          },
          scales: {
            x: { ticks: { font: { size: 10 } } },
            y: { beginAtZero: true, ticks: { font: { size: 10 } } }
          }
        }
      };

      stat.chartInstance?.destroy();
      stat.chartInstance = new Chart(ctx, config);
    });
  }

  getColorSet(labels: string[]): string[] {
    const colorMap: { [key: string]: string } = {
      'CANDIDAT': 'rgba(255, 206, 86, 0.7)',
      'PARTENAIRE_EXTERNE': 'rgba(75, 192, 192, 0.7)',
      'ADMINISTRATEUR': 'rgba(255, 99, 132, 0.7)',
      'RESPONSABLE_RH': 'rgba(54, 162, 235, 0.7)',
     
    };

    return labels.map(label => colorMap[label.toUpperCase()] || 'rgba(180,180,180,0.7)');
  }
}
