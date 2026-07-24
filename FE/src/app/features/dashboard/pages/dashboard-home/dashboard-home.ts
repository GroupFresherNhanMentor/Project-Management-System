import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../../core/services/dashboard';
import { ProjectService } from '../../../../core/services/project';
import { ProjectDto } from '../../../../core/models/project.model';
import {
  PersonalDashboardData,
  ProjectDashboardData,
  DevTaskItem,
  AdminDashboardData,
  DashboardActivityItem,
  TaskSearchRequest,
  ApiResponse,
  TaskSearchResponse
} from '../../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './dashboard-home.html'
})
export class DashboardHome implements OnInit {
  private dashboardService = inject(DashboardService);
  private projectService = inject(ProjectService);

  Math = Math;
  role = 'DEVELOPER';
  currentDashboardType: 'PERSONAL' | 'PROJECT' = 'PERSONAL';
  activeTab: 'OPEN' | 'COMPLETED' | 'OVERDUE' = 'OPEN';

  currentProjectId: string | undefined = undefined;
  currentUserId: string | undefined = undefined;

  devData = signal<PersonalDashboardData | null>(null);
  pmData = signal<ProjectDashboardData | null>(null);
  allTasks = signal<DevTaskItem[]>([]);
  adminStats = signal<AdminDashboardData | null>(null);
  recentActivities = signal<DashboardActivityItem[]>([]);
  projectList = signal<ProjectDto[]>([]);

  statusOrder = [
    { key: 'TODO', label: 'To Do', css: 'bg-slate-400 text-slate-400' },
    { key: 'IN_PROGRESS', label: 'In Progress', css: 'bg-blue-500 text-blue-500' },
    { key: 'TESTING', label: 'Testing', css: 'bg-amber-500 text-amber-500' },
    { key: 'DONE', label: 'Done', css: 'bg-emerald-500 text-emerald-500' }
  ];

  priorityOrder = [
    { key: 'CRITICAL', label: 'Critical', css: 'text-red-600 bg-red-600' },
    { key: 'HIGH', label: 'High', css: 'text-orange-500 bg-orange-500' },
    { key: 'MEDIUM', label: 'Medium', css: 'text-blue-500 bg-blue-500' },
    { key: 'LOW', label: 'Low', css: 'text-slate-400 bg-slate-400' }
  ];

  devTasks = computed(() => this.allTasks().filter(t => t.status.toLowerCase() !== 'done' && t.status.toLowerCase() !== 'completed'));
  devCompletedTasks = computed(() => this.allTasks().filter(t => t.status.toLowerCase() === 'done' || t.status.toLowerCase() === 'completed'));
  devOverdueTasks = computed(() => this.allTasks().filter(t => this.isOverdue(t.dueDate) && t.status.toLowerCase() !== 'done' && t.status.toLowerCase() !== 'completed'));

  ngOnInit(): void {
    if (typeof window === 'undefined') {
      return; // Skip fetching data on Server-Side Prerendering / SSR build
    }
    this.extractLocalStorageData();
    this.loadDashboardData();
    this.fetchTasksFromApi();
  }

  private extractLocalStorageData(): void {
    if (typeof window === 'undefined' || !window.localStorage) {
      return;
    }

    const savedProject = localStorage.getItem('pms_selected_project');
    if (savedProject) {
      this.currentProjectId = savedProject.replace(/"/g, '');
    }

    const savedUserJson = localStorage.getItem('pms_user');
    if (savedUserJson) {
      try {
        const userObj = JSON.parse(savedUserJson);
        this.currentUserId = userObj.id;
        if (userObj.role) this.role = userObj.role;
      } catch (e) {
        console.error('Lỗi phân tích cú pháp JSON từ localStorage pms_user:', e);
      }
    }
  }

  loadDashboardData(): void {
    if (this.role === 'ADMIN') {
      this.dashboardService.getAdminStats().subscribe({
        next: (res: ApiResponse<AdminDashboardData>) => {
          if (res && (res.isSuccess || res.success)) this.adminStats.set(res.data);
        }
      });
    }

    this.dashboardService.getPersonalStats().subscribe({
      next: (res: ApiResponse<PersonalDashboardData>) => {
        if (res && (res.isSuccess || res.success)) this.devData.set(res.data);
      }
    });

    // Tải danh sách dự án khả dụng cho Dropdown Select
    this.projectService.getProjects({ page: 0, size: 100 }).subscribe({
      next: (page) => {
        if (page && page.items) {
          this.projectList.set(page.items);
          if (!this.currentProjectId && page.items.length > 0) {
            this.currentProjectId = page.items[0].id;
          }
          if (this.currentProjectId) {
            this.fetchProjectStats(this.currentProjectId);
          }
        }
      }
    });

    this.fetchRecentActivities();
  }

  onProjectChange(newProjectId: string): void {
    this.currentProjectId = newProjectId;
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.setItem('pms_selected_project', newProjectId);
    }
    this.fetchProjectStats(newProjectId);
    this.fetchRecentActivities();
  }

  fetchProjectStats(projectId: string): void {
    this.dashboardService.getProjectStats(projectId).subscribe({
      next: (res: ApiResponse<ProjectDashboardData>) => {
        if (res && (res.isSuccess || res.success)) this.pmData.set(res.data);
      }
    });
  }

  fetchRecentActivities(): void {
    const projectId = this.role === 'ADMIN' ? undefined : this.currentProjectId;
    this.dashboardService.getRecentActivities(projectId, 10).subscribe({
      next: (res: ApiResponse<DashboardActivityItem[]>) => {
        if (res && (res.isSuccess || res.success) && res.data) {
          this.recentActivities.set(res.data);
        }
      }
    });
  }

  fetchTasksFromApi(): void {
    const searchPayload: TaskSearchRequest = {
      page: 0,
      size: 10,
      projectId: this.currentProjectId,
      assigneeId: this.currentUserId
    };

    this.dashboardService.searchTasks(searchPayload).subscribe({
      next: (res: ApiResponse<TaskSearchResponse>) => {
        if (res && (res.isSuccess || res.success) && res.data?.items) {
          const mappedItems: DevTaskItem[] = res.data.items.map((t: any) => {
            return {
              id: t.id,
              taskKey: t.taskKey || 'TASK',
              summary: t.summary || 'No Summary',
              dueDate: t.dueDate,
              priority: t.priority || 'MEDIUM',
              status: t.statusName || t.status || 'To Do',
              statusColor: t.statusColor || '#9AA1AC'
            };
          });
          this.allTasks.set(mappedItems);
        }
      },
      error: (err: unknown) => {
        console.error('Lỗi khi gọi API tìm kiếm Task:', err);
      }
    });
  }

  taskByStatusEntries(map: Record<string, number>): [string, number][] {
    return Object.entries(map);
  }

  getActionLabel(action: string): string {
    const map: Record<string, string> = {
      TASK_CREATED:     'đã tạo task',
      STATUS_CHANGED:   'đã chuyển trạng thái',
      PRIORITY_CHANGED: 'đã đổi mức độ ưu tiên',
      ASSIGNEE_CHANGED: 'đã thay đổi người xử lý',
      COMMENT_ADDED:    'đã thêm bình luận vào'
    };
    return map[action] || action;
  }

  isOverdue(dueDate: string | null): boolean {
    if (!dueDate) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const taskDate = new Date(dueDate);
    taskDate.setHours(0, 0, 0, 0);
    return taskDate.getTime() < today.getTime();
  }
}
