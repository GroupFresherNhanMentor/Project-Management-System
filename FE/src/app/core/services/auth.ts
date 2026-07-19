import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

import { API } from '../../configs/api-endpoints';
import { APP_CONSTANTS } from '../../configs/constants';
import { ApiResponse } from '../models/api.model';
import { LoginRequest, LoginResponse, RefreshTokenRequest, RefreshTokenResponse } from '../models/auth.model';
import { UserDto } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<ApiResponse<LoginResponse>>(API.auth.login, payload).pipe(
      map(r => r.data),
      tap(data => {
        this.store(APP_CONSTANTS.tokenKey, data.accessToken);
        this.store(APP_CONSTANTS.refreshTokenKey, data.refreshToken);
        this.store(APP_CONSTANTS.userKey, JSON.stringify(data.user));
      }),
    );
  }

  refresh(refreshToken: string): Observable<RefreshTokenResponse> {
    const payload: RefreshTokenRequest = { refreshToken };
    return this.http.post<ApiResponse<RefreshTokenResponse>>(API.auth.refresh, payload).pipe(
      map(r => r.data),
      tap(data => {
        this.store(APP_CONSTANTS.tokenKey, data.accessToken);
        this.store(APP_CONSTANTS.refreshTokenKey, data.refreshToken);
      }),
    );
  }

  logout(): void {
    this.clearAll();
    void this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    return this.load(APP_CONSTANTS.tokenKey);
  }

  getRefreshToken(): string | null {
    return this.load(APP_CONSTANTS.refreshTokenKey);
  }

  getCurrentUser(): UserDto | null {
    const raw = this.load(APP_CONSTANTS.userKey);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserDto;
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  clearAll(): void {
    this.remove(APP_CONSTANTS.tokenKey);
    this.remove(APP_CONSTANTS.refreshTokenKey);
    this.remove(APP_CONSTANTS.userKey);
  }

  private store(key: string, value: string): void {
    if (this.isBrowser()) globalThis.localStorage.setItem(key, value);
  }

  private load(key: string): string | null {
    return this.isBrowser() ? globalThis.localStorage.getItem(key) : null;
  }

  private remove(key: string): void {
    if (this.isBrowser()) globalThis.localStorage.removeItem(key);
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId) && typeof globalThis.localStorage !== 'undefined';
  }
}
