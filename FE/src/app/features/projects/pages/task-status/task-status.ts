import { Component, signal, computed, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { TaskStatusDto, TaskWorkflowDto, CreateTaskWorkflowRequest } from '../../../../core/models/task.model';
import { TaskService } from '../../../../core/services/task';
import { ToastService } from '../../../../core/services/toast';
import { AuthService } from '../../../../core/services/auth';
import { ProjectContextService } from '../../../../core/services/project-context';

@Component({
  selector: 'app-task-status',
  imports: [FormsModule],
  templateUrl: './task-status.html',
})
export class TaskStatusManagement implements OnInit {
  private readonly route          = inject(ActivatedRoute);
  private readonly taskService    = inject(TaskService);
  private readonly toast          = inject(ToastService);
  private readonly authService    = inject(AuthService);
  private readonly projectContext = inject(ProjectContextService);
  private readonly platformId     = inject(PLATFORM_ID);

  readonly isPm = computed(() => this.projectContext.currentUserProjectRole() === 'PM');

  private readonly projectId = this.route.parent?.snapshot.paramMap.get('id') ?? '';

  readonly statuses  = signal<TaskStatusDto[]>([]);
  readonly workflows = signal<TaskWorkflowDto[]>([]);
  readonly loading   = signal(true);

  // create form
  name      = '';
  color     = '#3B4FD9';
  isInitial = false;
  isFinal   = false;
  submitting = false;

  // inline transition editor
  readonly editingId        = signal<string | null>(null);
  readonly pendingNextIds   = signal<Set<string>>(new Set());
  readonly savingTransitions = signal(false);

  readonly statusMap = computed(() =>
    new Map(this.statuses().map(s => [s.id, s]))
  );

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    forkJoin([
      this.taskService.getTaskStatuses(this.projectId),
      this.taskService.getWorkflow(this.projectId),
    ]).subscribe({
      next: ([statuses, workflows]) => {
        this.statuses.set(statuses);
        this.workflows.set(workflows);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private reloadWorkflow(): void {
    this.taskService.getWorkflow(this.projectId).subscribe({
      next: w => this.workflows.set(w),
    });
  }

  nextStatusNames(statusId: string): string {
    const map = this.statusMap();
    const names = this.workflows()
      .filter(w => w.fromStatusId === statusId)
      .map(w => map.get(w.toStatusId)?.name)
      .filter((n): n is string => !!n);
    return names.length ? names.join(', ') : '—';
  }

  otherStatuses(excludeId: string): TaskStatusDto[] {
    return this.statuses().filter(s => s.id !== excludeId);
  }

  createStatus(): void {
    if (!this.name.trim() || this.submitting) return;
    this.submitting = true;
    this.taskService.createTaskStatus(this.projectId, {
      name: this.name.trim(),
      color: this.color || undefined,
      isInitial: this.isInitial,
      isFinal: this.isFinal,
    }).subscribe({
      next: created => {
        this.statuses.update(list => [...list, created]);
        this.name = '';
        this.color = '#3B4FD9';
        this.isInitial = false;
        this.isFinal = false;
        this.submitting = false;
        this.toast.success('Status created.');
      },
      error: (err: HttpErrorResponse) => { this.submitting = false; this.toast.error(err.error?.message ?? 'Failed to create status.'); },
    });
  }

  deleteStatus(id: string): void {
    this.taskService.deleteTaskStatus(this.projectId, id).subscribe({
      next: () => {
        this.statuses.update(list => list.filter(s => s.id !== id));
        this.workflows.update(list => list.filter(w => w.fromStatusId !== id && w.toStatusId !== id));
        if (this.editingId() === id) this.editingId.set(null);
        this.toast.success('Status deleted.');
      },
      error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Failed to delete status.'),
    });
  }

  startEditTransitions(statusId: string): void {
    if (this.editingId() === statusId) {
      this.editingId.set(null);
      return;
    }
    const current = new Set(
      this.workflows().filter(w => w.fromStatusId === statusId).map(w => w.toStatusId)
    );
    this.pendingNextIds.set(current);
    this.editingId.set(statusId);
  }

  toggleNext(toStatusId: string, checked: boolean): void {
    const next = new Set(this.pendingNextIds());
    if (checked) next.add(toStatusId); else next.delete(toStatusId);
    this.pendingNextIds.set(next);
  }

  isNextChecked(toStatusId: string): boolean {
    return this.pendingNextIds().has(toStatusId);
  }

  saveTransitions(fromStatusId: string): void {
    this.savingTransitions.set(true);
    const current = this.workflows().filter(w => w.fromStatusId === fromStatusId);
    const currentSet = new Set(current.map(w => w.toStatusId));
    const desired = this.pendingNextIds();

    const toDelete = current.filter(w => !desired.has(w.toStatusId));
    const toAdd: CreateTaskWorkflowRequest[] = [...desired]
      .filter(id => !currentSet.has(id))
      .map(toStatusId => ({ fromStatusId, toStatusId }));

    const deleteObs = toDelete.length > 0
      ? forkJoin(toDelete.map(w => this.taskService.deleteWorkflow(this.projectId, w.id)))
      : of([] as void[]);

    const addObs = toAdd.length > 0
      ? this.taskService.createWorkflow(this.projectId, toAdd)
      : of([]);

    forkJoin([deleteObs, addObs]).subscribe({
      next: () => {
        this.reloadWorkflow();
        this.editingId.set(null);
        this.savingTransitions.set(false);
        this.toast.success('Transitions saved.');
      },
      error: (err: HttpErrorResponse) => {
        this.savingTransitions.set(false);
        this.toast.error(err.error?.message ?? 'Failed to save transitions.');
      },
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  toggleActive(status: TaskStatusDto): void {
    const next = !status.isActive;
    this.taskService.updateTaskStatus(this.projectId, status.id, { isActive: next }).subscribe({
      next: updated => {
        this.statuses.update(list => list.map(s => s.id === updated.id ? updated : s));
        this.toast.success(next ? 'Status enabled.' : 'Status disabled.');
      },
      error: (err: HttpErrorResponse) => this.toast.error(err.error?.message ?? 'Failed to update status.'),
    });
  }
}
