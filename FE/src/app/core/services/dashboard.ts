import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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

  searchTasks(request: TaskSearchRequest): Observable<ApiResponse<TaskSearchResponse>> {
    let params = new HttpParams()
      .set('page', request.page)
      .set('size', request.size);
    if (request.projectId)  params = params.set('projectId',  request.projectId);
    if (request.sprintId)   params = params.set('sprintId',   request.sprintId);
    if (request.status)     params = params.set('statusId',   request.status);
    if (request.priority)   params = params.set('priority',   request.priority);
    if (request.assigneeId) params = params.set('assigneeId', request.assigneeId);
    if (request.keyword)    params = params.set('keyword',    request.keyword);
    return this.http.get<ApiResponse<TaskSearchResponse>>('/api/tasks/search', { params });
  }
}
