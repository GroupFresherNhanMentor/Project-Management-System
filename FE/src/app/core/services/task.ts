import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { API } from '../../configs/api-endpoints';
import { ApiResponse, PageResponse } from '../models/api.model';
import { TaskDto, CreateTaskRequest, UpdateTaskRequest, AssignTaskRequest, TaskSearchParams } from '../models/task.model';
import { TaskCommentDto, CreateCommentRequest, UpdateCommentRequest } from '../models/comment.model';
import { TaskActivityDto } from '../models/activity.model';
import { WorklogDto, CreateWorklogRequest, UpdateWorklogRequest } from '../models/worklog.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  searchTasks(params?: TaskSearchParams): Observable<PageResponse<TaskDto>> {
    let httpParams = new HttpParams();
    if (params?.project) httpParams = httpParams.set('project', params.project);
    if (params?.sprint) httpParams = httpParams.set('sprint', params.sprint);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.priority) httpParams = httpParams.set('priority', params.priority);
    if (params?.assignee) httpParams = httpParams.set('assignee', params.assignee);
    if (params?.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    return this.http
      .get<ApiResponse<PageResponse<TaskDto>>>(API.tasks.search, { params: httpParams })
      .pipe(map(r => r.data));
  }

  getTaskById(id: string): Observable<TaskDto> {
    return this.http.get<ApiResponse<TaskDto>>(API.tasks.byId(id)).pipe(map(r => r.data));
  }

  createTask(body: CreateTaskRequest): Observable<TaskDto> {
    return this.http.post<ApiResponse<TaskDto>>(API.tasks.base, body).pipe(map(r => r.data));
  }

  updateTask(id: string, body: UpdateTaskRequest): Observable<TaskDto> {
    return this.http.put<ApiResponse<TaskDto>>(API.tasks.byId(id), body).pipe(map(r => r.data));
  }

  assignTask(id: string, body: AssignTaskRequest): Observable<TaskDto> {
    return this.http.patch<ApiResponse<TaskDto>>(API.tasks.assign(id), body).pipe(map(r => r.data));
  }

  getComments(taskId: string, page = 0, size = 20): Observable<PageResponse<TaskCommentDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<TaskCommentDto>>>(API.tasks.comments(taskId), { params })
      .pipe(map(r => r.data));
  }

  addComment(taskId: string, body: CreateCommentRequest): Observable<TaskCommentDto> {
    return this.http
      .post<ApiResponse<TaskCommentDto>>(API.tasks.comments(taskId), body)
      .pipe(map(r => r.data));
  }

  updateComment(commentId: string, body: UpdateCommentRequest): Observable<TaskCommentDto> {
    return this.http
      .put<ApiResponse<TaskCommentDto>>(API.comments.byId(commentId), body)
      .pipe(map(r => r.data));
  }

  deleteComment(commentId: string): Observable<void> {
    return this.http.delete<void>(API.comments.byId(commentId));
  }

  getActivities(taskId: string, page = 0, size = 20): Observable<PageResponse<TaskActivityDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<TaskActivityDto>>>(API.tasks.activities(taskId), { params })
      .pipe(map(r => r.data));
  }

  getWorklogs(taskId: string, page = 0, size = 20): Observable<PageResponse<WorklogDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<WorklogDto>>>(API.tasks.worklogs(taskId), { params })
      .pipe(map(r => r.data));
  }

  addWorklog(taskId: string, body: CreateWorklogRequest): Observable<WorklogDto> {
    return this.http
      .post<ApiResponse<WorklogDto>>(API.tasks.worklogs(taskId), body)
      .pipe(map(r => r.data));
  }

  updateWorklog(worklogId: string, body: UpdateWorklogRequest): Observable<WorklogDto> {
    return this.http
      .put<ApiResponse<WorklogDto>>(API.worklogs.byId(worklogId), body)
      .pipe(map(r => r.data));
  }

  deleteWorklog(worklogId: string): Observable<void> {
    return this.http.delete<void>(API.worklogs.byId(worklogId));
  }
}
