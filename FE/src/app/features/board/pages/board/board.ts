import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { TaskDto, TaskStatusDto } from '../../../../core/models/task.model';
import { TaskService } from '../../../../core/services/task';
import { ProjectService } from '../../../../core/services/project';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-board',
  imports: [InitialsPipe],
  templateUrl: './board.html',
})
export class Board implements OnInit {
  private readonly router         = inject(Router);
  private readonly route          = inject(ActivatedRoute);
  private readonly taskService    = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly platformId     = inject(PLATFORM_ID);

  private readonly projectId      = this.resolveProjectId();

  readonly statuses       = signal<TaskStatusDto[]>([]);
  readonly tasks          = signal<TaskDto[]>([]);
  readonly loading        = signal(true);
  readonly activeOnly     = signal(true);
  readonly activeSprintId = signal<string | null>(null);

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.taskService.getTaskStatuses(this.projectId).subscribe({
      next: list => this.statuses.set(list.filter(s => s.isActive)),
    });
    this.projectService.getSprints(this.projectId, 0, 50).subscribe({
      next: res => {
        const active = res.items.find(s => s.status === 'ACTIVE') ?? null;
        this.activeSprintId.set(active?.id ?? null);
        this.loadTasks();
      },
      error: () => this.loadTasks(),
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

  private loadTasks(): void {
    const sprintId = this.activeOnly() && this.activeSprintId() ? this.activeSprintId()! : undefined;
    this.taskService.searchTasks({ projectId: this.projectId, sprintId, size: 100 }).subscribe({
      next: res => { this.tasks.set(res.items); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  setFilter(activeOnly: boolean): void {
    this.activeOnly.set(activeOnly);
    this.loading.set(true);
    this.loadTasks();
  }

  tasksFor(statusId: string): TaskDto[] {
    return this.tasks().filter(t => t.statusId === statusId);
  }

  openTask(task: TaskDto): void { void this.router.navigate(['/tasks', task.id]); }

  typeClass(type: string): string {
    return type === 'BUG' ? 'text-[#D0342C]' : type === 'STORY' ? 'text-[#2A5CD9]' : 'text-[#4B5160]';
  }

  priorityColor(p: string): string {
    return ({ LOW: '#5B6472', MEDIUM: '#2A5CD9', HIGH: '#C4720A', CRITICAL: '#D0342C' })[p] ?? '#9AA1AC';
  }
}
