import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth';
import { DashboardPersonalResponse, DashboardProjectResponse } from '../../../../core/models/dashboard.model';
import { TaskDto } from '../../../../core/models/task.model';

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
})
export class DashboardHome {
  readonly currentUser = inject(AuthService).getCurrentUser();
  readonly role        = this.currentUser?.role ?? 'USER';

  readonly adminStats = signal({ totalUsers: 6, lockedUsers: 1, totalProjects: 2, activeProjects: 1 });

  readonly pmData = signal<DashboardProjectResponse>({
    totalTasks: 12,
    taskByStatus:   { TODO: 5, IN_PROGRESS: 2, TESTING: 1, DONE: 4 },
    taskByPriority: { LOW: 3, MEDIUM: 3, HIGH: 4, CRITICAL: 2 },
    totalLoggedHours: 17,
    sprintProgress: { sprintId: 's1', sprintName: 'Sprint 12', totalTasks: 6, doneTasks: 1, percentComplete: 17 },
  });

  readonly pmMembers = signal(4);

  readonly devData = signal<DashboardPersonalResponse>({
    myOpenTasks: 3, myCompletedTasks: 2, myOverdueTasks: 1, totalLoggedHours: 11,
  });

  readonly devTasks = signal<TaskDto[]>([
    { id: 't1', taskKey: 'WEB-101', projectId: 'p1', sprintId: 's1', summary: 'Redesign checkout flow',           description: null, taskType: 'STORY', priority: 'HIGH',     status: 'IN_PROGRESS', assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 8,    estimateHour: 40,   dueDate: '2026-07-18', createdAt: '2026-07-01' },
    { id: 't2', taskKey: 'WEB-102', projectId: 'p1', sprintId: 's1', summary: 'Integrate payment gateway webhook', description: null, taskType: 'TASK',  priority: 'CRITICAL', status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 5,    estimateHour: null, dueDate: '2026-07-20', createdAt: '2026-07-01' },
    { id: 't5', taskKey: 'WEB-105', projectId: 'p1', sprintId: 's1', summary: 'Guest checkout',                   description: null, taskType: 'STORY', priority: 'MEDIUM',   status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-21', createdAt: '2026-07-02' },
  ]);

  readonly statusOrder = [
    { key: 'TODO',        label: 'To Do',      css: 'dot-todo' },
    { key: 'IN_PROGRESS', label: 'In Progress', css: 'dot-inprogress' },
    { key: 'TESTING',     label: 'Testing',     css: 'dot-testing' },
    { key: 'DONE',        label: 'Done',        css: 'dot-done' },
  ];

  readonly priorityOrder = [
    { key: 'LOW',      label: 'Low',      css: 'priority-low' },
    { key: 'MEDIUM',   label: 'Medium',   css: 'priority-medium' },
    { key: 'HIGH',     label: 'High',     css: 'priority-high' },
    { key: 'CRITICAL', label: 'Critical', css: 'priority-critical' },
  ];

  isOverdue(dueDate: string | null): boolean {
    if (!dueDate) return false;
    return new Date(dueDate) < new Date();
  }
}
