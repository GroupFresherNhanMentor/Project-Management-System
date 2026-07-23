import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { API } from '../../configs/api-endpoints';
import { ApiResponse, PageResponse } from '../models/api.model';
import { TaskDto, TaskStatusDto, TaskWorkflowDto, CreateTaskStatusRequest, UpdateTaskStatusRequest, CreateTaskWorkflowRequest, CreateTaskRequest, UpdateTaskRequest, AssignTaskRequest, TaskSearchParams } from '../models/task.model';
import { TaskCommentDto, CreateCommentRequest,UpdateCommentRequest } from '../models/comment.model';
import { TaskActivityDto } from '../models/activity.model';
import { WorklogDto, CreateWorklogRequest, UpdateWorklogRequest } from '../models/worklog.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  searchTasks(params?: TaskSearchParams): Observable<PageResponse<TaskDto>> {
    let httpParams = new HttpParams();
    if (params?.projectId)  httpParams = httpParams.set('projectId',  params.projectId);
    if (params?.sprintId)   httpParams = httpParams.set('sprintId',   params.sprintId);
    if (params?.statusId)   httpParams = httpParams.set('statusId',   params.statusId);
    if (params?.priority)   httpParams = httpParams.set('priority',   params.priority);
    if (params?.assigneeId) httpParams = httpParams.set('assigneeId', params.assigneeId);
    if (params?.keyword)    httpParams = httpParams.set('keyword',    params.keyword);
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    return this.http
      .get<ApiResponse<PageResponse<TaskDto>>>(API.tasks.search, { params: httpParams })
      .pipe(map(r => r.data));
  }

  getTaskStatuses(projectId: string, filters?: { isInitial?: boolean; isActive?: boolean }): Observable<TaskStatusDto[]> {
    let params = new HttpParams();
    if (filters?.isInitial != null) params = params.set('isInitial', filters.isInitial);
    if (filters?.isActive  != null) params = params.set('isActive',  filters.isActive);
    return this.http
      .get<ApiResponse<TaskStatusDto[]>>(API.taskStatuses.base(projectId), { params })
      .pipe(map(r => r.data));
  }

  createTaskStatus(projectId: string, body: CreateTaskStatusRequest): Observable<TaskStatusDto> {
    return this.http
      .post<ApiResponse<TaskStatusDto>>(API.taskStatuses.base(projectId), body)
      .pipe(map(r => r.data));
  }

  updateTaskStatus(projectId: string, id: string, body: UpdateTaskStatusRequest): Observable<TaskStatusDto> {
    return this.http
      .put<ApiResponse<TaskStatusDto>>(API.taskStatuses.byId(projectId, id), body)
      .pipe(map(r => r.data));
  }

  deleteTaskStatus(projectId: string, id: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(API.taskStatuses.byId(projectId, id))
      .pipe(map(() => void 0));
  }

  getWorkflow(projectId: string): Observable<TaskWorkflowDto[]> {
    return this.http
      .get<ApiResponse<TaskWorkflowDto[]>>(API.taskWorkflow.base(projectId))
      .pipe(map(r => r.data));
  }

  createWorkflow(projectId: string, entries: CreateTaskWorkflowRequest[]): Observable<TaskWorkflowDto[]> {
    return this.http
      .post<ApiResponse<TaskWorkflowDto[]>>(API.taskWorkflow.base(projectId), entries)
      .pipe(map(r => r.data));
  }

  deleteWorkflow(projectId: string, id: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(API.taskWorkflow.byId(projectId, id))
      .pipe(map(() => void 0));
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

  // updateComment(commentId: string, body: UpdateCommentRequest): Observable<TaskCommentDto> {
  //   return this.http
  //     .put<ApiResponse<TaskCommentDto>>(API.comments.byId(commentId), body)
  //     .pipe(map(r => r.data));
  // }

  // deleteComment(commentId: string): Observable<void> {
  //   return this.http.delete<void>(API.comments.byId(commentId));
  // }

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
