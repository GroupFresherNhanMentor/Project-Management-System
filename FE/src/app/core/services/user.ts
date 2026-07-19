import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { API } from '../../configs/api-endpoints';
import { ApiResponse, PageResponse } from '../models/api.model';
import { UserDto, CreateUserRequest, UpdateUserRequest, UpdateUserStatusRequest, UserListParams } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getUsers(params?: UserListParams): Observable<PageResponse<UserDto>> {
    let httpParams = new HttpParams();
    if (params?.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params?.role) httpParams = httpParams.set('role', params.role);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    return this.http
      .get<ApiResponse<PageResponse<UserDto>>>(API.users.base, { params: httpParams })
      .pipe(map(r => r.data));
  }

  getUserById(id: string): Observable<UserDto> {
    return this.http.get<ApiResponse<UserDto>>(API.users.byId(id)).pipe(map(r => r.data));
  }

  createUser(body: CreateUserRequest): Observable<UserDto> {
    return this.http.post<ApiResponse<UserDto>>(API.users.base, body).pipe(map(r => r.data));
  }

  updateUser(id: string, body: UpdateUserRequest): Observable<UserDto> {
    return this.http.put<ApiResponse<UserDto>>(API.users.byId(id), body).pipe(map(r => r.data));
  }

  updateUserStatus(id: string, body: UpdateUserStatusRequest): Observable<UserDto> {
    return this.http.patch<ApiResponse<UserDto>>(API.users.lock(id), body).pipe(map(r => r.data));
  }
}
