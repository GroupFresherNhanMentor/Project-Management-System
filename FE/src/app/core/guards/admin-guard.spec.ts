import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { adminGuard } from './admin-guard';
import { AuthService } from '../services/auth';

describe('adminGuard', () => {
  const dashboardTree = {} as UrlTree;
  const authService = { getCurrentUser: vi.fn() };
  const router = { createUrlTree: vi.fn(() => dashboardTree) };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('allows an administrator', () => {
    authService.getCurrentUser.mockReturnValue({ role: 'ADMIN' });

    const result = TestBed.runInInjectionContext(() => adminGuard(null!, null!));

    expect(result).toBe(true);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('redirects a non-administrator to the dashboard', () => {
    authService.getCurrentUser.mockReturnValue({ role: 'USER' });

    const result = TestBed.runInInjectionContext(() => adminGuard(null!, null!));

    expect(result).toBe(dashboardTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
  });
});
