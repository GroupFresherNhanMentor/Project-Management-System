import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  PersonalDashboardData,
  ProjectDashboardData,
  AdminDashboardData,
  TaskSearchRequest,
  TaskSearchResponse
} from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private baseUrl = '/api/dashboard';

  // Thống kê cá nhân /api/dashboard/me
  getPersonalStats(): Observable<ApiResponse<PersonalDashboardData>> {
    return this.http.get<ApiResponse<PersonalDashboardData>>(`${this.baseUrl}/me`);
  }

  // Thống kê dự án /api/dashboard/project/{id}
  getProjectStats(projectId: string): Observable<ApiResponse<ProjectDashboardData>> {
    return this.http.get<ApiResponse<ProjectDashboardData>>(`${this.baseUrl}/project/${projectId}`);
  }

  // Thống kê tổng quan hệ thống cho Admin /api/dashboard/admin
  getAdminStats(): Observable<ApiResponse<AdminDashboardData>> {
    return this.http.get<ApiResponse<AdminDashboardData>>(`${this.baseUrl}/admin`);
  }

  // Tìm kiếm Task phân trang từ database /api/tasks/search
  searchTasks(request: TaskSearchRequest): Observable<ApiResponse<TaskSearchResponse>> {
    return this.http.post<ApiResponse<TaskSearchResponse>>('/api/tasks/search', request);
  }
}
