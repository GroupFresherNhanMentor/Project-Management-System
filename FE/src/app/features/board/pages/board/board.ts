import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { inject } from '@angular/core';
import { TaskDto } from '../../../../core/models/task.model';
import { TaskStatus } from '../../../../core/models/api.model';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

const MOCK_TASKS: TaskDto[] = [
  { id: 't2', taskKey: 'WEB-102', projectId: 'p1', sprintId: 's1', summary: 'Integrate payment gateway webhook',    description: null, taskType: 'TASK',  priority: 'CRITICAL', status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 5,    estimateHour: null, dueDate: '2026-07-20', createdAt: '2026-07-01' },
  { id: 't5', taskKey: 'WEB-105', projectId: 'p1', sprintId: 's1', summary: 'Guest checkout',                       description: null, taskType: 'STORY', priority: 'MEDIUM',   status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-21', createdAt: '2026-07-02' },
  { id: 't1', taskKey: 'WEB-101', projectId: 'p1', sprintId: 's1', summary: 'Redesign checkout flow',               description: null, taskType: 'STORY', priority: 'HIGH',     status: 'IN_PROGRESS', assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 8,    estimateHour: 40,   dueDate: '2026-07-18', createdAt: '2026-07-01' },
  { id: 't6', taskKey: 'WEB-106', projectId: 'p1', sprintId: 's1', summary: 'Session expires mid-checkout',         description: null, taskType: 'BUG',   priority: 'CRITICAL', status: 'IN_PROGRESS', assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-22', createdAt: '2026-07-03' },
  { id: 't3', taskKey: 'WEB-103', projectId: 'p1', sprintId: 's1', summary: 'Cart total miscalculates with coupon', description: null, taskType: 'BUG',   priority: 'HIGH',     status: 'TESTING',     assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 3,    estimateHour: null, dueDate: '2026-07-19', createdAt: '2026-07-01' },
  { id: 't4', taskKey: 'WEB-104', projectId: 'p1', sprintId: 's1', summary: 'Add empty-state illustration to cart', description: null, taskType: 'TASK',  priority: 'LOW',      status: 'DONE',        assigneeId: 'u4', assigneeName: 'Mai Le',   reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 2,    estimateHour: null, dueDate: '2026-07-15', createdAt: '2026-07-01' },
];

const COLUMNS: Array<{ status: TaskStatus; label: string; dot: string }> = [
  { status: 'TODO',        label: 'To Do',      dot: 'dot-todo' },
  { status: 'IN_PROGRESS', label: 'In Progress', dot: 'dot-inprogress' },
  { status: 'TESTING',     label: 'Testing',     dot: 'dot-testing' },
  { status: 'DONE',        label: 'Done',        dot: 'dot-done' },
];

@Component({
  selector: 'app-board',
  imports: [InitialsPipe],
  templateUrl: './board.html',
})
export class Board {
  private readonly router = inject(Router);

  readonly activeOnly = signal(true);
  readonly tasks      = signal<TaskDto[]>(MOCK_TASKS);
  readonly columns    = COLUMNS;

  setFilter(activeOnly: boolean): void { this.activeOnly.set(activeOnly); }

  tasksFor(status: TaskStatus): TaskDto[] { return this.tasks().filter(t => t.status === status); }

  openTask(task: TaskDto): void { void this.router.navigate(['/tasks', task.id]); }

  typeClass(type: string): string {
    return type === 'BUG' ? 'text-[#D0342C]' : type === 'STORY' ? 'text-[#2A5CD9]' : 'text-[#4B5160]';
  }

  priorityClass(p: string): string {
    return ({ LOW: '#5B6472', MEDIUM: '#2A5CD9', HIGH: '#C4720A', CRITICAL: '#D0342C' })[p] ?? '#9AA1AC';
  }
}
