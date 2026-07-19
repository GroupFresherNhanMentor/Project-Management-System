import { Component, signal, inject } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { SprintDto } from '../../../../core/models/sprint.model';
import { TaskDto } from '../../../../core/models/task.model';

const MOCK_TASKS: TaskDto[] = [
  { id: 't1', taskKey: 'WEB-101', projectId: 'p1', sprintId: 's1', summary: 'Redesign checkout flow',               description: null, taskType: 'STORY', priority: 'HIGH',     status: 'IN_PROGRESS', assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 8,    estimateHour: 40,   dueDate: '2026-07-18', createdAt: '2026-07-01' },
  { id: 't2', taskKey: 'WEB-102', projectId: 'p1', sprintId: 's1', summary: 'Integrate payment gateway webhook',    description: null, taskType: 'TASK',  priority: 'CRITICAL', status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 5,    estimateHour: null, dueDate: '2026-07-20', createdAt: '2026-07-01' },
  { id: 't3', taskKey: 'WEB-103', projectId: 'p1', sprintId: 's1', summary: 'Cart total miscalculates with coupon', description: null, taskType: 'BUG',   priority: 'HIGH',     status: 'TESTING',     assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 3,    estimateHour: null, dueDate: '2026-07-19', createdAt: '2026-07-01' },
  { id: 't4', taskKey: 'WEB-104', projectId: 'p1', sprintId: 's1', summary: 'Add empty-state illustration to cart', description: null, taskType: 'TASK',  priority: 'LOW',      status: 'DONE',        assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 2,    estimateHour: null, dueDate: '2026-07-15', createdAt: '2026-07-01' },
  { id: 't5', taskKey: 'WEB-105', projectId: 'p1', sprintId: 's1', summary: 'Guest checkout',                       description: null, taskType: 'STORY', priority: 'MEDIUM',   status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-21', createdAt: '2026-07-02' },
  { id: 't6', taskKey: 'WEB-106', projectId: 'p1', sprintId: 's1', summary: 'Session expires mid-checkout',         description: null, taskType: 'BUG',   priority: 'CRITICAL', status: 'IN_PROGRESS', assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-22', createdAt: '2026-07-03' },
];

@Component({
  selector: 'app-sprint-detail',
  imports: [RouterLink],
  templateUrl: './sprint-detail.html',
})
export class SprintDetail {
  private readonly router = inject(Router);

  readonly sprint = signal<SprintDto>({
    id: 's1', projectId: 'p1', sprintName: 'Sprint 12', goal: 'Ship checkout redesign',
    startDate: '2026-07-07', endDate: '2026-07-21', status: 'ACTIVE',
  });

  readonly tasks = signal<TaskDto[]>(MOCK_TASKS);

  closeSprint(): void { this.sprint.update(s => ({ ...s, status: 'CLOSED' })); }
  startSprint(): void { this.sprint.update(s => ({ ...s, status: 'ACTIVE' })); }
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
