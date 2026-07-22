import { Component, inject, signal, computed } from '@angular/core'; // Thêm computed từ @angular/core
import { AuthService } from '../../../../core/services/auth';
import { DashboardPersonalResponse, DashboardProjectResponse } from '../../../../core/models/dashboard.model';
import { TaskDto } from '../../../../core/models/task.model';

export interface SystemActivityDto {
  id: string;
  taskKey: string;
  user: string;
  action: 'TASK_CREATED' | 'STATUS_CHANGED' | 'PRIORITY_CHANGED' | 'ASSIGNEE_CHANGED' | 'COMMENT_ADDED';
  oldValue: string | null;
  newValue: string | null;
  createdTime: string;
}

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
})
export class DashboardHome {
  readonly currentUser = inject(AuthService).getCurrentUser();
  readonly role        = this.currentUser?.role ?? 'USER';

  // Quản lý trạng thái Tab được chọn (mặc định ban đầu là 'OPEN')
  activeTab = 'OPEN';

  readonly adminStats = signal({
    totalUsers: 6,
    lockedUsers: 1,
    totalProjects: 2,
    activeProjects: 1,
    roleBreakdown: { PM: 2, DEV: 3, TESTER: 1 },
    taskDistribution: { STORY: 15, TASK: 22, BUG: 7 },
    priorityDistribution: { LOW: 10, MEDIUM: 18, HIGH: 12, CRITICAL: 4 }
  });

  readonly adminActivities = signal<SystemActivityDto[]>([
    { id: 'a1', taskKey: 'WEB-101', user: 'Nguyễn Đình Quân', action: 'STATUS_CHANGED', oldValue: 'IN_PROGRESS', newValue: 'TESTING', createdTime: '10 phút trước' },
    { id: 'a2', taskKey: 'WEB-102', user: 'Phạm Thái Sơn', action: 'COMMENT_ADDED', oldValue: null, newValue: 'Đã hoàn thành phần webhook', createdTime: '25 phút trước' },
    { id: 'a3', taskKey: 'WEB-105', user: 'Nguyễn Thị B', action: 'TASK_CREATED', oldValue: null, newValue: 'Guest checkout flow', createdTime: '1 giờ trước' },
    { id: 'a4', taskKey: 'WEB-103', user: 'Phạm Đăng Quang', action: 'ASSIGNEE_CHANGED', oldValue: 'Chưa giao', newValue: 'Nguyễn Văn A', createdTime: '2 giờ trước' }
  ]);

  getActionLabel(action: string): string {
    const map: Record<string, string> = {
      TASK_CREATED: 'đã tạo task',
      STATUS_CHANGED: 'đã chuyển trạng thái',
      PRIORITY_CHANGED: 'đã đổi mức độ ưu tiên',
      ASSIGNEE_CHANGED: 'đã thay đổi người xử lý',
      COMMENT_ADDED: 'đã thêm bình luận vào'
    };
    return map[action] || action;
  }

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

  readonly allMyTasks = signal<TaskDto[]>([
    { id: 't1', taskKey: 'WEB-101', projectId: 'p1', sprintId: 's1', summary: 'Redesign checkout flow',           description: null, taskType: 'STORY', priority: 'HIGH',     status: 'IN_PROGRESS', assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 8,    estimateHour: 40,   dueDate: '2026-07-18', createdAt: '2026-07-01' },
    { id: 't2', taskKey: 'WEB-102', projectId: 'p1', sprintId: 's1', summary: 'Integrate payment gateway webhook', description: null, taskType: 'TASK',  priority: 'CRITICAL', status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: 5,    estimateHour: null, dueDate: '2026-07-23', createdAt: '2026-07-01' },
    { id: 't3', taskKey: 'WEB-103', projectId: 'p1', sprintId: 's1', summary: 'Fix memory leaks in dashboard',     description: null, taskType: 'BUG',   priority: 'HIGH',     status: 'DONE',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-10', createdAt: '2026-07-01' },
    { id: 't4', taskKey: 'WEB-104', projectId: 'p1', sprintId: 's1', summary: 'Write unit tests for Auth module',  description: null, taskType: 'TASK',  priority: 'LOW',      status: 'DONE',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-15', createdAt: '2026-07-01' },
    { id: 't5', taskKey: 'WEB-105', projectId: 'p1', sprintId: 's1', summary: 'Guest checkout',                   description: null, taskType: 'STORY', priority: 'MEDIUM',   status: 'TODO',        assigneeId: 'u3', assigneeName: 'Huy Tran', reporterId: 'u2', reporterName: 'Lena Pham', storyPoint: null, estimateHour: null, dueDate: '2026-07-21', createdAt: '2026-07-02' },
  ]);

  // 1. Lọc công việc đang mở (Status KHÁC 'DONE')
  readonly devTasks = computed(() => {
    return this.allMyTasks().filter(t => t.status !== 'DONE');
  });

  // 2. Lọc công việc đã hoàn thành (Status LÀ 'DONE')[cite: 2, 4]
  readonly devCompletedTasks = computed(() => {
    return this.allMyTasks().filter(t => t.status === 'DONE');
  });

  // 3. Lọc công việc trễ hạn (Status KHÁC 'DONE' và quá ngày due_date)
  readonly devOverdueTasks = computed(() => {
    return this.devTasks().filter(t => this.isOverdue(t.dueDate));
  });

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

  // Đưa ngày đến hạn về mốc bắt đầu ngày (00:00:00)
  const taskDate = new Date(dueDate);
  taskDate.setHours(0, 0, 0, 0);

  // Đưa ngày hiện tại về mốc bắt đầu ngày (00:00:00)
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // Chỉ tính là overdue nếu ngày đến hạn nhỏ hơn hẳn ngày hôm nay
  return taskDate < today;
}
}
