import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { TaskDto, TaskStatusDto, TaskSearchParams } from '../../../../core/models/task.model';
import { SprintDto } from '../../../../core/models/sprint.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { TaskPriority } from '../../../../core/models/api.model';
import { TaskService } from '../../../../core/services/task';
import { ProjectService } from '../../../../core/services/project';

@Component({
  selector: 'app-backlog',
  imports: [FormsModule],
  templateUrl: './backlog.html',
})
export class Backlog implements OnInit {
  private readonly router         = inject(Router);
  private readonly route          = inject(ActivatedRoute);
  private readonly taskService    = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly platformId     = inject(PLATFORM_ID);

  private readonly projectId = this.resolveProjectId();

  readonly tasks        = signal<TaskDto[]>([]);
  readonly statuses     = signal<TaskStatusDto[]>([]);
  readonly sprints      = signal<SprintDto[]>([]);
  readonly members      = signal<ProjectMemberDto[]>([]);
  readonly loading      = signal(false);
  readonly error        = signal<string | null>(null);

  keyword = ''; statusId = ''; sprintId = ''; priority = ''; assigneeId = '';
  page = 0; size = 20;
  totalPages = 0;
  totalElements = 0;

  get totalPagesDisplay(): number { return this.totalPages || 1; }

  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loadStatuses();
    this.loadSprints();
    this.loadMembers();
    this.load();
  }

  load(): void {
    if (!this.projectId) return;

    const params: TaskSearchParams = {
      projectId: this.projectId,
      page: this.page,
      size: this.size,
      ...(this.keyword    && { keyword:    this.keyword }),
      ...(this.sprintId   && { sprintId:   this.sprintId }),
      ...(this.statusId   && { statusId:   this.statusId }),
      ...(this.priority   && { priority:   this.priority as TaskPriority }),
      ...(this.assigneeId && { assigneeId: this.assigneeId }),
    };

    this.loading.set(true);
    this.error.set(null);
    this.taskService.searchTasks(params).subscribe({
      next: res => {
        this.tasks.set(res.items);
        this.totalPages    = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load tasks. Please try again.');
        this.loading.set(false);
      },
    });
  }

  private loadStatuses(): void {
    this.taskService.getTaskStatuses(this.projectId).subscribe({
      next: list => this.statuses.set(list.filter(s => s.isActive)),
    });
  }

  private loadSprints(): void {
    this.projectService.getSprints(this.projectId, 0, 100).subscribe({
      next: res => this.sprints.set(res.items),
    });
  }

  private loadMembers(): void {
    this.projectService.getMembers(this.projectId, 0, 100).subscribe({
      next: res => this.members.set(res.items),
    });
  }

  private resolveProjectId(): string {
    let r: ActivatedRoute | null = this.route;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) return id;
      r = r.parent;
    }
    return '';
  }

  prevPage(): void { if (this.page > 0)                   { this.page--; this.load(); } }
  nextPage(): void { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }

  openTask(id: string): void { void this.router.navigate(['/tasks', id]); }
  newTask():            void { void this.router.navigate(['/projects', this.projectId, 'tasks', 'new']); }

  priorityColor(p: string): string {
    return ({ LOW: '#5B6472', MEDIUM: '#2A5CD9', HIGH: '#C4720A', CRITICAL: '#D0342C' } as Record<string, string>)[p] ?? '#9AA1AC';
  }
}
