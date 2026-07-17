import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

import { API_ENDPOINTS } from '../../configs/api-endpoints';
import { APP_CONSTANTS } from '../../configs/constants';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  accessToken?: string;
  token?: string;
  jwt?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  login(payload: LoginPayload): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_ENDPOINTS.auth}/login`, payload)
      .pipe(tap((response) => this.storeTokenFromResponse(response)));
  }

  register(payload: RegisterPayload): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_ENDPOINTS.auth}/register`, payload)
      .pipe(tap((response) => this.storeTokenFromResponse(response)));
  }

  logout(): void {
    this.clearToken();
    void this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    if (!this.canUseStorage()) {
      return null;
    }

    return globalThis.localStorage.getItem(APP_CONSTANTS.tokenKey);
  }

  clearToken(): void {
    if (!this.canUseStorage()) {
      return;
    }

    globalThis.localStorage.removeItem(APP_CONSTANTS.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private storeTokenFromResponse(response: AuthResponse): void {
    const token = response.accessToken ?? response.token ?? response.jwt;

    if (!token || !this.canUseStorage()) {
      return;
    }

    globalThis.localStorage.setItem(APP_CONSTANTS.tokenKey, token);
  }

  private canUseStorage(): boolean {
    return (
      isPlatformBrowser(this.platformId) &&
      typeof globalThis.localStorage !== 'undefined'
    );
  }
}
