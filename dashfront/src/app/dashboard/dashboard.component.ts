import { Component, OnInit, AfterViewInit } from '@angular/core';
import * as Chartist from 'chartist';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, AfterViewInit {

  totalForums = 0;
  totalOffres = 0;
  totalCandidats = 0;
  totalEntretiens = 0;

  constructor() {}

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  ngAfterViewInit(): void {
    this.initCharts();
  }

  loadDashboardStats(): void {
    // À remplacer par appels API / services réels
    this.totalForums = 6;
    this.totalOffres = 24;
    this.totalCandidats = 85;
    this.totalEntretiens = 12;
  }

  initCharts(): void {
    // Daily Sales Chart
    const dataDailySalesChart = {
      labels: ['L', 'M', 'M', 'J', 'V', 'S', 'D'],
      series: [[12, 17, 7, 17, 23, 18, 38]]
    };
    const optionsDailySalesChart = {
      interpolation: Chartist.Interpolation.cardinal({ tension: 0 }),
      low: 0,
      high: 50,
      chartPadding: { top: 0, right: 0, bottom: 0, left: 0 }
    } as any;

    const dailySalesChart = new Chartist.Line('#dailySalesChart', dataDailySalesChart, optionsDailySalesChart);
    this.startAnimationForLineChart(dailySalesChart);

    // Completed Tasks Chart
    const dataCompletedTasksChart = {
      labels: ['12p', '3p', '6p', '9p', '12a', '3a', '6a', '9a'],
      series: [[230, 750, 450, 300, 280, 240, 200, 190]]
    };
    const optionsCompletedTasksChart = {
      interpolation: Chartist.Interpolation.cardinal({ tension: 0 }),
      low: 0,
      high: 1000,
      chartPadding: { top: 0, right: 0, bottom: 0, left: 0 }
    } as any;

    const completedTasksChart = new Chartist.Line('#completedTasksChart', dataCompletedTasksChart, optionsCompletedTasksChart);
    this.startAnimationForLineChart(completedTasksChart);

    // Website Views Chart (Bar chart)
    const dataWebsiteViewsChart = {
      labels: ['J', 'F', 'M', 'A', 'M', 'J', 'J', 'A', 'S', 'O', 'N', 'D'],
      series: [[542, 443, 320, 780, 553, 453, 326, 434, 568, 610, 756, 895]]
    };
    const optionsWebsiteViewsChart: Chartist.IBarChartOptions = {
      axisX: { showGrid: false },
      low: 0,
      high: 1000,
      chartPadding: { top: 0, right: 5, bottom: 0, left: 0 }
    };
    const responsiveOptions: [string, Chartist.IBarChartOptions][] = [
      ['screen and (max-width: 640px)', {
        seriesBarDistance: 5,
        axisX: {
          labelInterpolationFnc: (value: string) => value[0]
        }
      }]
    ];

    const websiteViewsChart = new Chartist.Bar('#websiteViewsChart', dataWebsiteViewsChart, optionsWebsiteViewsChart, responsiveOptions);
    this.startAnimationForBarChart(websiteViewsChart);
  }

  startAnimationForLineChart(chart: any): void {
    let seq = 0;
    const delays = 80;
    const durations = 500;

    chart.on('draw', (data: any) => {
      if (data.type === 'line' || data.type === 'area') {
        data.element.animate({
          d: {
            begin: 600,
            dur: 700,
            from: data.path.clone().scale(1, 0).translate(0, data.chartRect.height()).stringify(),
            to: data.path.clone().stringify(),
            easing: Chartist.Svg.Easing.easeOutQuint
          }
        });
      } else if (data.type === 'point') {
        seq++;
        data.element.animate({
          opacity: {
            begin: seq * delays,
            dur: durations,
            from: 0,
            to: 1,
            easing: 'ease'
          }
        });
      }
    });

    // Réinitialiser seq après animation pour éviter conflits
    setTimeout(() => { seq = 0; }, delays * 100);
  }

  startAnimationForBarChart(chart: any): void {
    let seq = 0;
    const delays = 80;
    const durations = 500;

    chart.on('draw', (data: any) => {
      if (data.type === 'bar') {
        seq++;
        data.element.animate({
          opacity: {
            begin: seq * delays,
            dur: durations,
            from: 0,
            to: 1,
            easing: 'ease'
          }
        });
      }
    });

    setTimeout(() => { seq = 0; }, delays * 100);
  }

  // Boutons partage réseaux sociaux
  shareOnLinkedIn(): void {
    const url = encodeURIComponent(window.location.href);
    const text = encodeURIComponent(`Découvrez le tableau de bord de gestion des forums de recrutement Lyance !`);
    window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${url}&summary=${text}`, '_blank');
  }

  shareOnFacebook(): void {
    const url = encodeURIComponent(window.location.href);
    window.open(`https://www.facebook.com/sharer/sharer.php?u=${url}`, '_blank');
  }

  shareOnTwitter(): void {
    const text = encodeURIComponent(`Dashboard Lyance - Statistiques et gestion des forums de recrutement !`);
    const url = encodeURIComponent(window.location.href);
    window.open(`https://twitter.com/intent/tweet?text=${text}&url=${url}`, '_blank');
  }

}
