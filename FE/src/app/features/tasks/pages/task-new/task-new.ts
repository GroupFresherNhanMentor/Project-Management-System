import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { TaskType, TaskPriority } from '../../../../core/models/api.model';
import { TaskStatusDto, CreateTaskRequest } from '../../../../core/models/task.model';
import { SprintDto } from '../../../../core/models/sprint.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { TaskService } from '../../../../core/services/task';
import { ProjectService } from '../../../../core/services/project';
import { AuthService } from '../../../../core/services/auth';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-task-new',
  imports: [FormsModule],
  templateUrl: './task-new.html',
})
export class TaskNew implements OnInit {
  private readonly router         = inject(Router);
  private readonly route          = inject(ActivatedRoute);
  private readonly taskService    = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly authService    = inject(AuthService);
  private readonly toast          = inject(ToastService);
  private readonly platformId     = inject(PLATFORM_ID);

  private readonly projectId = this.resolveProjectId();

  private resolveProjectId(): string {
    let r: ActivatedRoute | null = this.route;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) return id;
      r = r.parent;
    }
    return '';
  }

  readonly statuses    = signal<TaskStatusDto[]>([]);
  readonly activeSprint = signal<SprintDto | null>(null);
  readonly members     = signal<ProjectMemberDto[]>([]);

  readonly taskTypes:  TaskType[]     = ['STORY', 'TASK', 'BUG'];
  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  taskKey      = '';
  summary      = '';
  description  = '';
  taskType: TaskType     = 'TASK';
  priority: TaskPriority = 'MEDIUM';
  statusId     = '';
  assigneeId   = '';
  sprintId     = '';
  storyPoint:   number | null = null;
  estimateHour: number | null = null;
  dueDate      = '';
  submitting   = false;

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.taskService.getTaskStatuses(this.projectId, { isInitial: true, isActive: true }).subscribe({
      next: list => {
        this.statuses.set(list);
        this.statusId = list[0]?.id ?? '';
      },
    });
    this.projectService.getSprints(this.projectId, 0, 50).subscribe({
      next: res => {
        const active = res.items.find(s => s.status === 'ACTIVE') ?? null;
        this.activeSprint.set(active);
      },
    });
    this.projectService.getMembers(this.projectId, 0, 100).subscribe({
      next: res => this.members.set(res.items),
    });
  }

  submit(): void {
    if (!this.taskKey.trim() || !this.summary.trim() || !this.statusId) return;
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    const body: CreateTaskRequest = {
      taskKey:      this.taskKey.trim(),
      projectId:    this.projectId,
      summary:      this.summary.trim(),
      taskType:     this.taskType,
      priority:     this.priority,
      taskStatusId: this.statusId,
      reporterId:   currentUser.id,
      ...(this.sprintId                && { sprintId:     this.sprintId }),
      ...(this.assigneeId              && { assigneeId:   this.assigneeId }),
      ...(this.description.trim()      && { description:  this.description.trim() }),
      ...(this.storyPoint   != null    && { storyPoint:   this.storyPoint }),
      ...(this.estimateHour != null    && { estimateHour: this.estimateHour }),
      ...(this.dueDate                 && { dueDate:      this.dueDate }),
    };

    this.submitting = true;
    this.taskService.createTask(body).subscribe({
      next: task => {
        this.toast.success('Task created.');
        void this.router.navigate(['/tasks', task.id]);
      },
      error: () => {
        this.toast.error('Failed to create task.');
        this.submitting = false;
      },
    });
  }

  cancel(): void {
    const projectId = this.projectId;
    if (projectId) {
      void this.router.navigate(['/projects', projectId, 'backlog']);
    } else {
      void this.router.navigate(['/dashboard']);
    }
  }
}
