import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { API } from '../../configs/api-endpoints';
import { ApiResponse, PageResponse } from '../models/api.model';
import { ProjectDto, CreateProjectRequest, UpdateProjectRequest, ProjectListParams } from '../models/project.model';
import {
  ProjectMemberDto,
  ProjectMemberCandidateDto,
  AddProjectMemberRequest,
} from '../models/project-member.model';
import { SprintDto, CreateSprintRequest, UpdateSprintStatusRequest } from '../models/sprint.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);

  getProjects(params?: ProjectListParams): Observable<PageResponse<ProjectDto>> {
    let httpParams = new HttpParams();
    if (params?.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    return this.http
      .get<ApiResponse<PageResponse<ProjectDto>>>(API.projects.base, { params: httpParams })
      .pipe(map(r => r.data));
  }

  getProjectById(id: string): Observable<ProjectDto> {
    return this.http.get<ApiResponse<ProjectDto>>(API.projects.byId(id)).pipe(map(r => r.data));
  }

  createProject(body: CreateProjectRequest): Observable<ProjectDto> {
    return this.http.post<ApiResponse<ProjectDto>>(API.projects.base, body).pipe(map(r => r.data));
  }

  updateProject(id: string, body: UpdateProjectRequest): Observable<ProjectDto> {
    return this.http.put<ApiResponse<ProjectDto>>(API.projects.byId(id), body).pipe(map(r => r.data));
  }

  getMembers(projectId: string, page = 0, size = 20): Observable<PageResponse<ProjectMemberDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<ProjectMemberDto>>>(API.projects.members(projectId), { params })
      .pipe(map(r => r.data));
  }

  getMemberCandidates(
    projectId: string,
    page = 0,
    size = 100,
  ): Observable<PageResponse<ProjectMemberCandidateDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<ProjectMemberCandidateDto>>>(
        API.projects.memberCandidates(projectId),
        { params },
      )
      .pipe(map(r => r.data));
  }

  addMember(projectId: string, body: AddProjectMemberRequest): Observable<ProjectMemberDto> {
    return this.http
      .post<ApiResponse<ProjectMemberDto>>(API.projects.members(projectId), body)
      .pipe(map(r => r.data));
  }

  removeMember(projectId: string, memberId: string): Observable<void> {
    return this.http.delete<void>(API.projects.memberById(projectId, memberId));
  }

  getSprints(projectId: string, page = 0, size = 20): Observable<PageResponse<SprintDto>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<SprintDto>>>(API.projects.sprints(projectId), { params })
      .pipe(map(r => r.data));
  }

  createSprint(projectId: string, body: CreateSprintRequest): Observable<SprintDto> {
    return this.http
      .post<ApiResponse<SprintDto>>(API.projects.sprints(projectId), body)
      .pipe(map(r => r.data));
  }

  updateSprintStatus(sprintId: string, body: UpdateSprintStatusRequest): Observable<SprintDto> {
    return this.http
      .patch<ApiResponse<SprintDto>>(API.sprints.status(sprintId), body)
      .pipe(map(r => r.data));
  }
}
