import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../../../core/services/dashboard';
import {
  PersonalDashboardData,
  ProjectDashboardData,
  DevTaskItem,
  AdminDashboardData,
  SystemActivity,
  TaskSearchRequest,
  ApiResponse,
  TaskSearchResponse
} from '../../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard-home.html'
})
export class DashboardHome implements OnInit {
  private dashboardService = inject(DashboardService);

  role = 'DEVELOPER';
  currentDashboardType: 'PERSONAL' | 'PROJECT' = 'PERSONAL';
  activeTab: 'OPEN' | 'COMPLETED' | 'OVERDUE' = 'OPEN';

  currentProjectId: string | undefined = undefined;
  currentUserId: string | undefined = undefined;

  devData = signal<PersonalDashboardData | null>(null);
  pmData = signal<ProjectDashboardData | null>(null);
  allTasks = signal<DevTaskItem[]>([]);

  adminStats = signal<AdminDashboardData | null>(null);

  statusOrder = [
    { key: 'TODO', label: 'To Do', css: 'bg-slate-400' },
    { key: 'IN_PROGRESS', label: 'In Progress', css: 'bg-blue-500' },
    { key: 'TESTING', label: 'Testing', css: 'bg-amber-500' },
    { key: 'DONE', label: 'Done', css: 'bg-emerald-500' }
  ];

  priorityOrder = [
    { key: 'CRITICAL', label: 'Critical', css: 'text-red-600 bg-red-600' },
    { key: 'HIGH', label: 'High', css: 'text-orange-500 bg-orange-500' },
    { key: 'MEDIUM', label: 'Medium', css: 'text-blue-500 bg-blue-500' },
    { key: 'LOW', label: 'Low', css: 'text-slate-400 bg-slate-400' }
  ];

  devTasks = computed(() => this.allTasks().filter(t => t.status === 'TODO' || t.status === 'IN_PROGRESS' || t.status === 'TESTING'));
  devCompletedTasks = computed(() => this.allTasks().filter(t => t.status === 'DONE'));
  devOverdueTasks = computed(() => this.allTasks().filter(t => this.isOverdue(t.dueDate) && t.status !== 'DONE'));

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

    if (this.currentProjectId) {
      this.dashboardService.getProjectStats(this.currentProjectId).subscribe({
        next: (res: ApiResponse<ProjectDashboardData>) => {
          if (res && (res.isSuccess || res.success)) this.pmData.set(res.data);
        }
      });
    }
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
            this.allTasks.set(res.data.items);
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
    return new Date(dueDate).getTime() < new Date().getTime();
  }

  getStatusLabel(statusKey: string): string {
    const status = this.statusOrder.find(s => s.key === statusKey);
    return status ? status.label : statusKey;
  }
}
