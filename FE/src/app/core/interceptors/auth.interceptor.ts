import { HttpInterceptorFn, HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';
import { AuthService } from '../services/auth';
import { API } from '../../configs/api-endpoints';

let isRefreshing = false;
const refreshSubject = new BehaviorSubject<string | null>(null);

function isAuthEndpoint(url: string): boolean {
  return url === API.auth.login || url === API.auth.refresh;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const authReq = token && !isAuthEndpoint(req.url)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || isAuthEndpoint(req.url)) {
        return throwError(() => error);
      }

      const refreshToken = authService.getRefreshToken();
      if (!refreshToken) {
        authService.logout();
        return throwError(() => error);
      }

      if (!isRefreshing) {
        isRefreshing = true;
        refreshSubject.next(null);

        authService.refresh(refreshToken).subscribe({
          next: (res) => {
            isRefreshing = false;
            refreshSubject.next(res.accessToken);
          },
          error: () => {
            isRefreshing = false;
            refreshSubject.next(null);
            authService.logout();
          },
        });
      }

      return refreshSubject.pipe(
        filter((newToken) => newToken !== null || isRefreshing === false),
        take(1),
        switchMap((newToken) => {
          if (!newToken) return throwError(() => error);
          const retryReq = req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } });
          return next(retryReq);
        }),
      );
    }),
  );
};
