import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { ProjectDto } from '../../../../core/models/project.model';
import { ProjectStatus } from '../../../../core/models/api.model';
import { ProjectService } from '../../../../core/services/project';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-project-list',
  imports: [FormsModule],
  templateUrl: './project-list.html',
})
export class ProjectList implements OnInit {
  private readonly authService    = inject(AuthService);
  private readonly router         = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly toast          = inject(ToastService);
  private readonly platformId     = inject(PLATFORM_ID);

  readonly projects = signal<ProjectDto[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly isAdmin  = this.authService.getCurrentUser()?.role === 'ADMIN';
  readonly statuses: ProjectStatus[] = ['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED'];

  keyword = '';
  status: ProjectStatus | '' = '';
  page = 0;
  readonly size = 12;
  totalPages = 0;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) this.load();
  }

  load(page = this.page): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.projectService.getProjects({
      keyword: this.keyword.trim() || undefined,
      status: this.status || undefined,
      page,
      size: this.size,
    }).subscribe({
      next: result => {
        this.projects.set(result.items);
        this.page = result.pageNumber;
        this.totalPages = result.totalPages;
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(this.errorText(error, 'Unable to load projects.'));
        this.loading.set(false);
      },
    });
  }

  applyFilters(): void { this.load(0); }
  previousPage(): void { if (this.page > 0) this.load(this.page - 1); }
  nextPage(): void { if (this.page + 1 < this.totalPages) this.load(this.page + 1); }

  newProject(): void { void this.router.navigate(['/projects/new']); }
  viewProject(project: ProjectDto): void { void this.router.navigate(['/projects', project.id]); }

  statusBadge(s: string): string {
    const m: Record<string, string> = { PLANNING: 'badge-planned', ACTIVE: 'badge-active', ON_HOLD: 'badge-todo', COMPLETED: 'badge-done' };
    return 'badge ' + (m[s] ?? '');
  }

  statusLabel(s: string): string {
    return ({ PLANNING: 'Planning', ACTIVE: 'Active', ON_HOLD: 'On Hold', COMPLETED: 'Completed' })[s] ?? s;
  }

  private errorText(error: HttpErrorResponse, fallback: string): string {
    const message = error.error?.message as string | undefined;
    if (message) this.toast.error(message);
    return message ?? fallback;
  }
}
