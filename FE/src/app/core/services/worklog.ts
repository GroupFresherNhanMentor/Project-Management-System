import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { API } from '../../configs/api-endpoints';
import { ApiResponse, PageResponse } from '../models/api.model';
import { WorklogReportItem, WorklogReportParams } from '../models/worklog.model';

@Injectable({ providedIn: 'root' })
export class WorklogService {
  private readonly http = inject(HttpClient);

  getReport(params?: WorklogReportParams): Observable<PageResponse<WorklogReportItem>> {
    let httpParams = new HttpParams();
    if (params?.project) httpParams = httpParams.set('project', params.project);
    if (params?.user) httpParams = httpParams.set('user', params.user);
    if (params?.fromDate) httpParams = httpParams.set('fromDate', params.fromDate);
    if (params?.toDate) httpParams = httpParams.set('toDate', params.toDate);
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    return this.http
      .get<ApiResponse<PageResponse<WorklogReportItem>>>(API.reports.worklog, { params: httpParams })
      .pipe(map(r => r.data));
  }
}
