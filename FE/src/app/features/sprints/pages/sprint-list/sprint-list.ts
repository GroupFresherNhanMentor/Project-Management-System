import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { SprintDto, CreateSprintRequest, UpdateSprintRequest, UpdateSprintStatusRequest } from '../../../../core/models/sprint.model';
import { ProjectService } from '../../../../core/services/project';
import { ToastService } from '../../../../core/services/toast';
import { AuthService } from '../../../../core/services/auth';
import { SprintStatus } from '../../../../core/models/api.model';

@Component({
  selector: 'app-sprint-list',
  imports: [FormsModule],
  templateUrl: './sprint-list.html',
})
export class SprintList {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly toast = inject(ToastService);
  private readonly authService = inject(AuthService);

  readonly sprints = signal<SprintDto[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  // ProjectId lấy từ parent route: /projects/:id/sprints
  readonly projectId = signal<string | null>(null);

  // Modal state
  readonly showModal = signal(false);
  readonly modalMode = signal<'create' | 'edit'>('create');
  readonly modalLoading = signal(false);
  // For create
  createForm = { sprintName: '', goal: '', startDate: '', endDate: '' };
  // For edit
  readonly editingSprint = signal<SprintDto | null>(null);
  editForm = { sprintName: '', goal: '', startDate: '', endDate: '' };

  // For status change
  readonly statusLoading = signal<string | null>(null);

  readonly canManage = signal(this.authService.getCurrentUser()?.role === 'ADMIN');
  readonly isAdmin = this.authService.getCurrentUser()?.role === 'ADMIN';

  constructor() {
    let r: ActivatedRoute | null = this.route;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) {
        this.projectId.set(id);
        if (!this.isAdmin) {
          // Check if user is PM
          this.projectService.getCurrentMember(id).subscribe({
            next: membership => this.canManage.set(membership.status === 'ACTIVE' && membership.projectRole === 'PM'),
            error: () => this.canManage.set(false),
          });
        }
        this.load(id);
        break;
      }
      r = r.parent;
    }
  }

  load(projectId = this.projectId()): void {
    if (!projectId) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.projectService.getSprints(projectId, 0, 100).subscribe({
      next: result => {
        this.sprints.set(result.items);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(err.error?.message ?? 'Unable to load sprints.');
        this.loading.set(false);
      },
    });
  }

  // ── Create Sprint ──
  openNewSprintModal(): void {
    this.modalMode.set('create');
    this.createForm = { sprintName: '', goal: '', startDate: '', endDate: '' };
    this.showModal.set(true);
  }

  submitCreate(): void {
    const projectId = this.projectId();
    if (!projectId || !this.createForm.sprintName || !this.createForm.startDate || !this.createForm.endDate) {
      this.toast.error('Please fill in all required fields.');
      return;
    }
    this.modalLoading.set(true);
    const body: CreateSprintRequest = {
      sprintName: this.createForm.sprintName,
      goal: this.createForm.goal || undefined,
      startDate: this.createForm.startDate,
      endDate: this.createForm.endDate,
    };
    this.projectService.createSprint(projectId, body)
      .pipe(finalize(() => this.modalLoading.set(false)))
      .subscribe({
        next: () => {
          this.toast.success('Sprint created successfully.');
          this.showModal.set(false);
          this.load(projectId);
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to create sprint.'),
      });
  }

  // ── Edit Sprint ──
  openEditSprintModal(sprint: SprintDto, event: Event): void {
    event.stopPropagation();
    this.modalMode.set('edit');
    this.editingSprint.set(sprint);
    this.editForm = {
      sprintName: sprint.sprintName,
      goal: sprint.goal ?? '',
      startDate: sprint.startDate,
      endDate: sprint.endDate,
    };
    this.showModal.set(true);
  }

  submitEdit(): void {
    const projectId = this.projectId();
    const sprint = this.editingSprint();
    if (!projectId || !sprint) return;

    this.modalLoading.set(true);
    const body: UpdateSprintRequest = {};
    if (this.editForm.sprintName !== sprint.sprintName) body.sprintName = this.editForm.sprintName;
    if (this.editForm.goal !== (sprint.goal ?? '')) body.goal = this.editForm.goal || undefined;
    if (this.editForm.startDate !== sprint.startDate) body.startDate = this.editForm.startDate;
    if (this.editForm.endDate !== sprint.endDate) body.endDate = this.editForm.endDate;

    // Nếu không có gì thay đổi thì đóng modal
    if (Object.keys(body).length === 0) {
      this.showModal.set(false);
      return;
    }

    this.projectService.updateSprint(projectId, sprint.id, body)
      .pipe(finalize(() => this.modalLoading.set(false)))
      .subscribe({
        next: () => {
          this.toast.success('Sprint updated successfully.');
          this.showModal.set(false);
          this.load(projectId);
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to update sprint.'),
      });
  }

  // ── Status transitions ──
  startSprint(sprint: SprintDto, event: Event): void {
    event.stopPropagation();
    const projectId = this.projectId();
    if (!projectId) return;
    this.statusLoading.set(sprint.id);
    this.projectService.updateSprintStatus(projectId, sprint.id, { status: 'ACTIVE' })
      .pipe(finalize(() => this.statusLoading.set(null)))
      .subscribe({
        next: () => {
          this.toast.success(`"${sprint.sprintName}" started.`);
          this.load(projectId);
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to start sprint.'),
      });
  }

  closeSprint(sprint: SprintDto, event: Event): void {
    event.stopPropagation();
    const projectId = this.projectId();
    if (!projectId) return;
    this.statusLoading.set(sprint.id);
    this.projectService.updateSprintStatus(projectId, sprint.id, { status: 'CLOSED' })
      .pipe(finalize(() => this.statusLoading.set(null)))
      .subscribe({
        next: () => {
          this.toast.success(`"${sprint.sprintName}" closed.`);
          this.load(projectId);
        },
        error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Unable to close sprint.'),
      });
  }

  // ── Utils ──
  cancelModal(): void { this.showModal.set(false); }

  statusBadge(status: string): string {
    const m: Record<string, string> = { PLANNED: 'badge-planned', ACTIVE: 'badge-active', CLOSED: 'badge-closed' };
    return 'badge ' + (m[status] ?? '');
  }
}
