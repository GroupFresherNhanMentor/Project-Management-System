import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { SprintDto } from '../../../../core/models/sprint.model';
import { TaskDto, TaskStatusDto } from '../../../../core/models/task.model';
import { TaskService } from '../../../../core/services/task';
import { ProjectService } from '../../../../core/services/project';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-sprint-detail',
  imports: [],
  templateUrl: './sprint-detail.html',
})
export class SprintDetail implements OnInit {
  private readonly router         = inject(Router);
  private readonly route          = inject(ActivatedRoute);
  private readonly taskService    = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly toast          = inject(ToastService);
  private readonly platformId     = inject(PLATFORM_ID);

  private readonly projectId = this.resolveProjectId();
  private readonly sprintId  = this.route.snapshot.paramMap.get('id') ?? '';

  readonly sprint   = signal<SprintDto | null>(null);
  readonly tasks    = signal<TaskDto[]>([]);
  readonly statuses = signal<TaskStatusDto[]>([]);
  readonly isPm     = signal(false);
  readonly loading  = signal(true);
  readonly error    = signal<string | null>(null);

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loadSprint();
    this.loadStatuses();
    this.loadTasks();
    this.projectService.getCurrentMember(this.projectId).subscribe({
      next: member => this.isPm.set(member.projectRole === 'PM'),
    });
  }

  private resolveProjectId(): string {
    let r: ActivatedRoute | null = this.route.parent;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) return id;
      r = r.parent;
    }
    return '';
  }

  private loadSprint(): void {
    this.projectService.getSprintById(this.projectId, this.sprintId).subscribe({
      next: s => { this.sprint.set(s); this.loading.set(false); },
      error: () => { this.error.set('Failed to load sprint.'); this.loading.set(false); },
    });
  }

  private loadStatuses(): void {
    this.taskService.getTaskStatuses(this.projectId, { isActive: true }).subscribe({
      next: list => this.statuses.set(list),
    });
  }

  private loadTasks(): void {
    this.taskService.searchTasks({ projectId: this.projectId, sprintId: this.sprintId, size: 200 }).subscribe({
      next: res => this.tasks.set(res.items),
    });
  }

  startSprint(): void {
    this.projectService.updateSprintStatus(this.projectId, this.sprintId, { status: 'ACTIVE' }).subscribe({
      next: s => { this.sprint.set(s); this.toast.success('Sprint started.'); },
      error: () => this.toast.error('Failed to start sprint.'),
    });
  }

  closeSprint(): void {
    this.projectService.updateSprintStatus(this.projectId, this.sprintId, { status: 'CLOSED' }).subscribe({
      next: s => { this.sprint.set(s); this.toast.success('Sprint closed.'); },
      error: () => this.toast.error('Failed to close sprint.'),
    });
  }

  openTask(id: string): void { void this.router.navigate(['/tasks', id]); }

  statusBadge(s: string): string {
    return 'badge ' + ({ PLANNED: 'badge-planned', ACTIVE: 'badge-active', CLOSED: 'badge-closed' }[s] ?? '');
  }

  priorityColor(p: string): string {
    return ({ LOW: '#5B6472', MEDIUM: '#2A5CD9', HIGH: '#C4720A', CRITICAL: '#D0342C' })[p] ?? '#9AA1AC';
  }

  get doneCount(): number {
    const finalIds = new Set(this.statuses().filter(s => s.isFinal).map(s => s.id));
    return this.tasks().filter(t => finalIds.has(t.statusId)).length;
  }
}
