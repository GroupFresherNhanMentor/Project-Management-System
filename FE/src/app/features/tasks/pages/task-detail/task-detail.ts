import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Location } from '@angular/common';
import { ToastService } from '../../../../core/services/toast';
import { TaskDto } from '../../../../core/models/task.model';
import { TaskCommentDto } from '../../../../core/models/comment.model';
import { TaskActivityDto } from '../../../../core/models/activity.model';
import { WorklogDto } from '../../../../core/models/worklog.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { TaskStatus, TaskPriority } from '../../../../core/models/api.model';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

type Tab = 'details' | 'comments' | 'worklog' | 'activity';

const MOCK_TASK: TaskDto = {
  id: 't1', taskKey: 'WEB-101', projectId: 'p1', sprintId: 's1',
  summary: 'Redesign checkout flow',
  description: 'Complete overhaul of the 3-step checkout: cart review → shipping → payment. Needs to match the new design system and support both guest and authenticated flows.',
  taskType: 'STORY', priority: 'HIGH', status: 'IN_PROGRESS',
  assigneeId: 'u3', assigneeName: 'Huy Tran',
  reporterId: 'u2', reporterName: 'Lena Pham',
  storyPoint: 8, estimateHour: 40,
  dueDate: '2026-07-18', createdAt: '2026-07-01',
};

const MOCK_MEMBERS: ProjectMemberDto[] = [
  { id: 'm1', projectId: 'p1', userId: 'u2', userFullName: 'Lena Pham',   projectRole: 'PM',     status: 'ACTIVE' },
  { id: 'm2', projectId: 'p1', userId: 'u3', userFullName: 'Huy Tran',    projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm3', projectId: 'p1', userId: 'u4', userFullName: 'Mai Le',      projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm4', projectId: 'p1', userId: 'u5', userFullName: 'Khoa Nguyen', projectRole: 'TESTER', status: 'ACTIVE' },
];

const MOCK_COMMENTS: TaskCommentDto[] = [
  { id: 'c1', taskId: 't1', createdBy: 'Lena Pham',    content: 'Designs approved. Ready to start implementation.', createdTime: '2026-07-01 09:00' },
  { id: 'c2', taskId: 't1', createdBy: 'Huy Tran',     content: 'Starting on the cart review step today.',          createdTime: '2026-07-08 10:30' },
  { id: 'c3', taskId: 't1', createdBy: 'Khoa Nguyen',  content: 'QA checklist ready for when you hit testing.',    createdTime: '2026-07-10 14:15' },
];

const MOCK_ACTIVITIES: TaskActivityDto[] = [
  { id: 'a1', taskId: 't1', action: 'TASK_CREATED',     userId: 'u2', userName: 'Lena Pham',    oldValue: null,   newValue: null,          createdTime: '2026-07-01 09:00' },
  { id: 'a2', taskId: 't1', action: 'ASSIGNEE_CHANGED', userId: 'u2', userName: 'Lena Pham',    oldValue: null,   newValue: 'Huy Tran',    createdTime: '2026-07-01 09:05' },
  { id: 'a3', taskId: 't1', action: 'STATUS_CHANGED',   userId: 'u3', userName: 'Huy Tran',     oldValue: 'TODO', newValue: 'IN_PROGRESS', createdTime: '2026-07-08 10:00' },
  { id: 'a4', taskId: 't1', action: 'COMMENT_ADDED',    userId: 'u5', userName: 'Khoa Nguyen',  oldValue: null,   newValue: null,          createdTime: '2026-07-10 14:15' },
];

const MOCK_WORKLOGS: WorklogDto[] = [
  { id: 'wl1', taskId: 't1', workDate: '2026-07-14', hour: 4, description: 'Implemented new cart layout', createdBy: 'u3' },
  { id: 'wl2', taskId: 't1', workDate: '2026-07-15', hour: 6, description: 'Connected step components',   createdBy: 'u3' },
];

@Component({
  selector: 'app-task-detail',
  imports: [FormsModule, InitialsPipe],
  templateUrl: './task-detail.html',
})
export class TaskDetail {
  private readonly toast    = inject(ToastService);
  private readonly location = inject(Location);

  readonly task       = signal<TaskDto>(MOCK_TASK);
  readonly comments   = signal<TaskCommentDto[]>(MOCK_COMMENTS);
  readonly activities = signal<TaskActivityDto[]>(MOCK_ACTIVITIES);
  readonly worklogs   = signal<WorklogDto[]>(MOCK_WORKLOGS);
  readonly members    = signal<ProjectMemberDto[]>(MOCK_MEMBERS);

  activeTab: Tab = 'details';
  readonly tabs: Tab[] = ['details', 'comments', 'worklog', 'activity'];

  editStatus: TaskStatus     = MOCK_TASK.status;
  editPriority: TaskPriority = MOCK_TASK.priority;
  editAssigneeId  = MOCK_TASK.assigneeId ?? '';
  editDescription = MOCK_TASK.description ?? '';
  editEstimateHour: number | null = MOCK_TASK.estimateHour;
  editDueDate     = MOCK_TASK.dueDate ?? '';
  editStoryPoint: number | null = MOCK_TASK.storyPoint;

  newComment      = '';
  newWorklogDate  = '';
  newWorklogHours: number | null = null;
  newWorklogDesc  = '';

  readonly allStatuses: TaskStatus[]   = ['TODO', 'IN_PROGRESS', 'TESTING', 'DONE'];
  readonly allPriorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  goBack(): void { this.location.back(); }

  saveDetails(): void {
    this.task.update(t => ({
      ...t,
      status:       this.editStatus,
      priority:     this.editPriority,
      assigneeId:   this.editAssigneeId,
      assigneeName: this.members().find(m => m.userId === this.editAssigneeId)?.userFullName ?? t.assigneeName,
      description:  this.editDescription,
      estimateHour: this.editEstimateHour,
      dueDate:      this.editDueDate,
      storyPoint:   this.editStoryPoint,
    }));
    this.toast.success('Task updated.');
  }

  postComment(): void {
    if (!this.newComment.trim()) return;
    const c: TaskCommentDto = {
      id: crypto.randomUUID(), taskId: 't1',
      createdBy: 'Admin User',
      content: this.newComment,
      createdTime: new Date().toLocaleString(),
    };
    this.comments.update(list => [...list, c]);
    this.newComment = '';
  }

  addWorklog(): void {
    if (!this.newWorklogDate || !this.newWorklogHours) { this.toast.error('Date and hours are required.'); return; }
    const w: WorklogDto = {
      id: crypto.randomUUID(), taskId: 't1',
      workDate: this.newWorklogDate, hour: this.newWorklogHours,
      description: this.newWorklogDesc || null,
      createdBy: 'u1',
    };
    this.worklogs.update(list => [w, ...list]);
    this.newWorklogDate = ''; this.newWorklogHours = null; this.newWorklogDesc = '';
    this.toast.success('Worklog added.');
  }

  isMyWorklog(_wl: WorklogDto): boolean { return true; }

  deleteWorklog(wl: WorklogDto): void {
    this.worklogs.update(list => list.filter(w => w.id !== wl.id));
    this.toast.success('Deleted.');
  }

  typeLabel(t: string): string { return ({ STORY: 'Story', TASK: 'Task', BUG: 'Bug' })[t] ?? t; }

  statusLabel(s: string): string {
    return ({ TODO: 'To Do', IN_PROGRESS: 'In Progress', TESTING: 'Testing', DONE: 'Done' })[s] ?? s;
  }

  activityLabel(a: TaskActivityDto): string {
    const who = a.userName ?? 'Someone';
    switch (a.action) {
      case 'TASK_CREATED':     return `${who} created this task`;
      case 'STATUS_CHANGED':   return `${who} changed status to ${a.newValue}`;
      case 'PRIORITY_CHANGED': return `${who} changed priority to ${a.newValue}`;
      case 'ASSIGNEE_CHANGED': return `${who} changed assignee to ${a.newValue}`;
      case 'COMMENT_ADDED':    return `${who} added a comment`;
      default: return `${who} performed action`;
    }
  }
}
