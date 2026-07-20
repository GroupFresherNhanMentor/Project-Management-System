import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskDto } from '../../../../core/models/task.model';
import { SprintDto } from '../../../../core/models/sprint.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';

const MOCK_TASKS: TaskDto[] = [
  { id: 't1', taskKey: 'WEB-101', projectId: 'p1', sprintId: 's1', summary: 'Redesign checkout flow',               description: null, taskType: 'STORY', priority: 'HIGH',     status: 'IN_PROGRESS', assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 8,    estimateHour: 40,   dueDate: '2026-07-18', createdAt: '2026-07-01' },
  { id: 't2', taskKey: 'WEB-102', projectId: 'p1', sprintId: 's1', summary: 'Integrate payment gateway webhook',    description: null, taskType: 'TASK',  priority: 'CRITICAL', status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 5,    estimateHour: null, dueDate: '2026-07-20', createdAt: '2026-07-01' },
  { id: 't3', taskKey: 'WEB-103', projectId: 'p1', sprintId: 's1', summary: 'Cart total miscalculates with coupon', description: null, taskType: 'BUG',   priority: 'HIGH',     status: 'TESTING',     assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 3,    estimateHour: null, dueDate: '2026-07-19', createdAt: '2026-07-01' },
  { id: 't4', taskKey: 'WEB-104', projectId: 'p1', sprintId: 's1', summary: 'Add empty-state illustration to cart', description: null, taskType: 'TASK',  priority: 'LOW',      status: 'DONE',        assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 2,    estimateHour: null, dueDate: '2026-07-15', createdAt: '2026-07-01' },
  { id: 't5', taskKey: 'WEB-105', projectId: 'p1', sprintId: 's1', summary: 'Guest checkout',                       description: null, taskType: 'STORY', priority: 'MEDIUM',   status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-21', createdAt: '2026-07-02' },
  { id: 't6', taskKey: 'WEB-106', projectId: 'p1', sprintId: 's1', summary: 'Session expires mid-checkout',         description: null, taskType: 'BUG',   priority: 'CRITICAL', status: 'IN_PROGRESS', assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-22', createdAt: '2026-07-03' },
];

const MOCK_SPRINTS: SprintDto[] = [
  { id: 's1', projectId: 'p1', sprintName: 'Sprint 12', goal: 'Ship checkout redesign', startDate: '2026-07-07', endDate: '2026-07-21', status: 'ACTIVE' },
  { id: 's2', projectId: 'p1', sprintName: 'Sprint 11', goal: 'Complete auth revamp',   startDate: '2026-06-23', endDate: '2026-07-06', status: 'CLOSED' },
  { id: 's3', projectId: 'p1', sprintName: 'Sprint 13', goal: 'TBD',                    startDate: '2026-07-22', endDate: '2026-08-04', status: 'PLANNED' },
];

const MOCK_MEMBERS: ProjectMemberDto[] = [
  { id: 'm1', projectId: 'p1', userId: 'u2', userFullName: 'Lena Pham',   projectRole: 'PM',     status: 'ACTIVE' },
  { id: 'm2', projectId: 'p1', userId: 'u3', userFullName: 'Huy Tran',    projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm3', projectId: 'p1', userId: 'u4', userFullName: 'Mai Le',      projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm4', projectId: 'p1', userId: 'u5', userFullName: 'Khoa Nguyen', projectRole: 'TESTER', status: 'ACTIVE' },
];

@Component({
  selector: 'app-backlog',
  imports: [FormsModule],
  templateUrl: './backlog.html',
})
export class Backlog {
  private readonly router = inject(Router);

  readonly sprints = signal(MOCK_SPRINTS);
  readonly members = signal(MOCK_MEMBERS);

  keyword = ''; sprint = ''; status = ''; priority = ''; assignee = '';
  page = 0; size = 20;
  get totalPages() { return Math.ceil(this.tasks().length / this.size) || 1; }

  tasks(): TaskDto[] {
    return MOCK_TASKS.filter(t =>
      (!this.keyword  || t.summary.toLowerCase().includes(this.keyword.toLowerCase())) &&
      (!this.sprint   || t.sprintId === this.sprint) &&
      (!this.status   || t.status   === this.status) &&
      (!this.priority || t.priority === this.priority) &&
      (!this.assignee || t.assigneeId === this.assignee),
    );
  }

  load(): void { /* filtering is reactive via tasks() */ }

  prevPage(): void { if (this.page > 0) this.page--; }
  nextPage(): void { if (this.page < this.totalPages - 1) this.page++; }

  openTask(id: string): void { void this.router.navigate(['/tasks', id]); }
  newTask(): void          { void this.router.navigate(['/tasks/new']); }

  statusLabel(s: string): string {
    return ({ TODO: 'To Do', IN_PROGRESS: 'In Progress', TESTING: 'Testing', DONE: 'Done' })[s] ?? s;
  }
  statusBadge(s: string): string {
    const m: Record<string, string> = { TODO: 'badge-todo', IN_PROGRESS: 'badge-inprogress', TESTING: 'badge-testing', DONE: 'badge-done' };
    return 'badge ' + (m[s] ?? '');
  }
  priorityColor(p: string): string {
    return ({ LOW: '#5B6472', MEDIUM: '#2A5CD9', HIGH: '#C4720A', CRITICAL: '#D0342C' })[p] ?? '#9AA1AC';
  }
}
