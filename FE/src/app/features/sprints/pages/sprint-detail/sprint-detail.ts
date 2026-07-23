import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { SprintDto, UpdateSprintStatusRequest } from '../../../../core/models/sprint.model';
import { TaskDto } from '../../../../core/models/task.model';
import { ProjectService } from '../../../../core/services/project';
import { TaskService } from '../../../../core/services/task';
import { ToastService } from '../../../../core/services/toast';
import { AuthService } from '../../../../core/services/auth';
import { SprintStatus } from '../../../../core/models/api.model';

@Component({
  selector: 'app-sprint-detail',
  imports: [],
  templateUrl: './sprint-detail.html',
})
export class SprintDetail {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly taskService = inject(TaskService);
  private readonly toast = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly sprint = signal<SprintDto | null>(null);
  readonly tasks = signal<TaskDto[]>([]);
  readonly loading = signal(true);
  readonly statusLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly canManage = signal(this.authService.getCurrentUser()?.role === 'ADMIN');

  private projectId = '';
  private sprintId = '';

  constructor() {
    // Route: /projects/:id/sprints/:sprintId
    this.sprintId = this.route.snapshot.paramMap.get('id') ?? '';

    // Get projectId from parent route
    let r: ActivatedRoute | null = this.route.parent;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) { this.projectId = id; break; }
      r = r.parent;
    }

    if (!this.projectId || !this.sprintId) {
      this.errorMessage.set('Invalid sprint URL.');
      this.loading.set(false);
      return;
    }

    this.loadData();
    this.resolvePermissions();
  }

  private resolvePermissions(): void {
    const user = this.authService.getCurrentUser();
    if (user?.role === 'ADMIN') {
      this.canManage.set(true);
      return;
    }
    this.projectService.getCurrentMember(this.projectId).subscribe({
      next: membership => this.canManage.set(membership.status === 'ACTIVE' && membership.projectRole === 'PM'),
      error: () => this.canManage.set(false),
    });
  }

  private loadData(): void {
    this.loading.set(true);
    this.projectService.getSprintById(this.projectId, this.sprintId).subscribe({
      next: sprint => {
        this.sprint.set(sprint);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Sprint not found.');
        this.loading.set(false);
      },
    });

    // Load tasks for this sprint
    this.taskService.searchTasks({ sprint: this.sprintId, size: 200 }).subscribe({
      next: result => this.tasks.set(result.items),
      error: () => {/* non-critical */},
    });
  }

  startSprint(): void {
    const s = this.sprint();
    if (!s) return;
    this.statusLoading.set(true);
    this.projectService.updateSprintStatus(this.projectId, this.sprintId, { status: 'ACTIVE' })
      .pipe(finalize(() => this.statusLoading.set(false)))
      .subscribe({
        next: updated => {
          this.sprint.set(updated);
          this.toast.success(`"${s.sprintName}" started.`);
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to start sprint.'),
      });
  }

  closeSprint(): void {
    const s = this.sprint();
    if (!s) return;
    this.statusLoading.set(true);
    this.projectService.updateSprintStatus(this.projectId, this.sprintId, { status: 'CLOSED' })
      .pipe(finalize(() => this.statusLoading.set(false)))
      .subscribe({
        next: updated => {
          this.sprint.set(updated);
          this.toast.success(`"${s.sprintName}" closed.`);
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to close sprint.'),
      });
  }

  openTask(id: string): void { void this.router.navigate(['/tasks', id]); }

  statusBadge(s: string): string {
    return 'badge ' + ({ PLANNED: 'badge-planned', ACTIVE: 'badge-active', CLOSED: 'badge-closed' }[s] ?? '');
  }
  taskStatusBadge(s: string): string {
    return 'badge ' + ({ TODO: 'badge-todo', IN_PROGRESS: 'badge-inprogress', TESTING: 'badge-testing', DONE: 'badge-done' }[s] ?? '');
  }
  taskStatusLabel(s: string): string {
    return ({ TODO: 'To Do', IN_PROGRESS: 'In Progress', TESTING: 'Testing', DONE: 'Done' })[s] ?? s;
  }
  priorityColor(p: string): string {
    return ({ LOW: '#5B6472', MEDIUM: '#2A5CD9', HIGH: '#C4720A', CRITICAL: '#D0342C' })[p] ?? '#9AA1AC';
  }
  get doneCount(): number { return this.tasks().filter(t => t.status === 'DONE').length; }
}
