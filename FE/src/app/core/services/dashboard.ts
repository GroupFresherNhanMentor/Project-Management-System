import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { API } from '../../configs/api-endpoints';
import { ApiResponse } from '../models/api.model';
import { DashboardPersonalResponse, DashboardProjectResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  getPersonalDashboard(): Observable<DashboardPersonalResponse> {
    return this.http
      .get<ApiResponse<DashboardPersonalResponse>>(API.dashboard.me)
      .pipe(map(r => r.data));
  }

  getProjectDashboard(projectId: string): Observable<DashboardProjectResponse> {
    return this.http
      .get<ApiResponse<DashboardProjectResponse>>(API.dashboard.project(projectId))
      .pipe(map(r => r.data));
  }
}
