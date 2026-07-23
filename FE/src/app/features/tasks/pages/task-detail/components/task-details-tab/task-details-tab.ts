import { Component, signal, computed, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { TaskService } from '../../../../../../core/services/task';
import { ProjectService } from '../../../../../../core/services/project';
import { ToastService } from '../../../../../../core/services/toast';
import { TaskDetailService } from '../../task-detail.service';
import { TaskDto, TaskStatusDto, TaskWorkflowDto, UpdateTaskRequest } from '../../../../../../core/models/task.model';
import { ProjectMemberDto } from '../../../../../../core/models/project-member.model';

@Component({
  selector: 'app-task-details-tab',
  imports: [FormsModule],
  templateUrl: './task-details-tab.html',
})
export class TaskDetailsTab implements OnInit {
  private readonly route          = inject(ActivatedRoute);
  private readonly taskService    = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly toast          = inject(ToastService);
  readonly taskSvc                = inject(TaskDetailService);
  private readonly platformId     = inject(PLATFORM_ID);

  private get taskId(): string {
    return this.route.parent?.snapshot.paramMap.get('id') ?? '';
  }

  readonly statuses  = signal<TaskStatusDto[]>([]);
  readonly workflows = signal<TaskWorkflowDto[]>([]);
  readonly members   = signal<ProjectMemberDto[]>([]);
  readonly isPm      = signal(false);
  readonly saving    = signal(false);

  readonly availableStatuses = computed(() => {
    if (this.isPm()) return this.statuses();
    const currentStatusId = this.taskSvc.task()?.statusId;
    if (!currentStatusId) return this.statuses();
    const nextIds = new Set(
      this.workflows()
        .filter(w => w.fromStatusId === currentStatusId)
        .map(w => w.toStatusId)
    );
    return this.statuses().filter(s => s.id === currentStatusId || nextIds.has(s.id));
  });

  editStatusId    = '';
  editAssigneeId  = '';
  editDescription = '';
  editEstimateHour: number | null = null;
  editDueDate     = '';

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const task = this.taskSvc.task();
    if (task) {
      this.initFields(task);
      this.loadFormData(task.projectId);
    } else {
      this.taskService.getTaskById(this.taskId).subscribe({
        next: t => {
          this.taskSvc.task.set(t);
          this.initFields(t);
          this.loadFormData(t.projectId);
        },
      });
    }
  }

  private initFields(t: TaskDto): void {
    this.editStatusId     = t.statusId;
    this.editAssigneeId   = t.assigneeId ?? '';
    this.editDescription  = t.description ?? '';
    this.editEstimateHour = t.estimateHour;
    this.editDueDate      = t.dueDate ?? '';
  }

  private loadFormData(projectId: string): void {
    forkJoin([
      this.taskService.getTaskStatuses(projectId, { isActive: true }),
      this.taskService.getWorkflow(projectId),
      this.projectService.getCurrentMember(projectId),
      this.projectService.getMembers(projectId, 0, 100),
    ]).subscribe({
      next: ([statuses, workflows, member, membersPage]) => {
        this.statuses.set(statuses.filter(s => s.isActive));
        this.workflows.set(workflows);
        this.isPm.set(member.projectRole === 'PM');
        this.members.set(membersPage.items);
      },
    });
  }

  saveDetails(): void {
    const task = this.taskSvc.task();
    if (!task) return;
    const prevAssigneeId = task.assigneeId ?? '';
    const assigneeChanged = this.editAssigneeId !== prevAssigneeId;
    const body: UpdateTaskRequest = {
      statusId:     this.editStatusId    || undefined,
      description:  this.editDescription || undefined,
      estimateHour: this.editEstimateHour ?? undefined,
      dueDate:      this.editDueDate     || undefined,
    };

    this.saving.set(true);
    this.taskService.updateTask(this.taskId, body).subscribe({
      next: updated => {
        this.taskSvc.task.set(updated);
        this.toast.success('Task updated.');
        if (assigneeChanged) {
          this.taskService.assignTask(this.taskId, {
            assigneeId: this.editAssigneeId || null,
          }).subscribe({
            next: t => this.taskSvc.task.set(t),
            error: () => this.toast.error('Failed to update assignee.'),
          });
        }
        this.saving.set(false);
      },
      error: () => {
        this.toast.error('Failed to update task.');
        this.saving.set(false);
      },
    });
  }
}
