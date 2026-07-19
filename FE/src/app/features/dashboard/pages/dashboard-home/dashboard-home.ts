import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../../core/services/dashboard';
import { DashboardPersonalResponse } from '../../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-home',
  imports: [CommonModule],
  templateUrl: './dashboard-home.html',
})
export class DashboardHome implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  readonly data = signal<DashboardPersonalResponse | null>(null);

  ngOnInit(): void {
    this.dashboardService.getPersonalDashboard().subscribe({
      next: res => this.data.set(res),
    });
  }
}
