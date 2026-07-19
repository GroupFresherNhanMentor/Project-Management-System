import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { ProjectDto } from '../../../../core/models/project.model';

const MOCK_PROJECTS: ProjectDto[] = [
  { id: 'p1', projectCode: 'WEB', projectName: 'Website Revamp',   description: 'Full redesign of the customer-facing storefront.',  startDate: '2026-06-01', endDate: '2026-09-30', status: 'ACTIVE' },
  { id: 'p2', projectCode: 'MOB', projectName: 'Mobile App',       description: 'Native iOS & Android companion app.',               startDate: '2026-07-01', endDate: '2026-12-31', status: 'PLANNING' },
  { id: 'p3', projectCode: 'INF', projectName: 'Infra Migration',  description: 'Move to Kubernetes and managed cloud services.',    startDate: '2026-05-15', endDate: '2026-08-15', status: 'ON_HOLD' },
];

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.html',
})
export class ProjectList {
  private readonly authService = inject(AuthService);
  private readonly router      = inject(Router);

  readonly projects = signal<ProjectDto[]>(MOCK_PROJECTS);
  readonly isAdmin  = this.authService.getCurrentUser()?.role === 'ADMIN';

  newProject(): void { void this.router.navigate(['/projects/new']); }

  statusBadge(s: string): string {
    const m: Record<string, string> = { PLANNING: 'badge-planned', ACTIVE: 'badge-active', ON_HOLD: 'badge-todo', COMPLETED: 'badge-done' };
    return 'badge ' + (m[s] ?? '');
  }

  statusLabel(s: string): string {
    return ({ PLANNING: 'Planning', ACTIVE: 'Active', ON_HOLD: 'On Hold', COMPLETED: 'Completed' })[s] ?? s;
  }
}
