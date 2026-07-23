import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap, Router, UrlTree } from '@angular/router';
import { firstValueFrom, of, throwError } from 'rxjs';

import { AuthService } from '../services/auth';
import { ProjectContextService } from '../services/project-context';
import { ProjectService } from '../services/project';
import { projectViewGuard } from './project-view-guard';

describe('projectViewGuard', () => {
  const dashboardTree = {} as UrlTree;
  const authService = { getCurrentUser: vi.fn() };
  const projectService = {
    getProjects: vi.fn(),
    getProjectById: vi.fn(),
  };
  const projectContext = { selectedProjectId: vi.fn(() => null) };
  const router = { createUrlTree: vi.fn(() => dashboardTree) };

  beforeEach(() => {
    vi.clearAllMocks();
    projectContext.selectedProjectId.mockReturnValue(null);
    TestBed.configureTestingModule({
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: AuthService, useValue: authService },
        { provide: ProjectService, useValue: projectService },
        { provide: ProjectContextService, useValue: projectContext },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('allows an administrator without calling project APIs', () => {
    authService.getCurrentUser.mockReturnValue({ role: 'ADMIN' });

    const result = runGuard(route());

    expect(result).toBe(true);
    expect(projectService.getProjects).not.toHaveBeenCalled();
  });

  it('allows a user whose PM-scoped project list is not empty', async () => {
    authService.getCurrentUser.mockReturnValue({ role: 'USER' });
    projectService.getProjects.mockReturnValue(of({ totalElements: 1 }));

    const result = await firstValueFrom(runGuard(route()) as ReturnType<typeof of>);

    expect(result).toBe(true);
    expect(projectService.getProjects).toHaveBeenCalledWith({ page: 0, size: 1 });
  });

  it('redirects a user with no PM-scoped projects', async () => {
    authService.getCurrentUser.mockReturnValue({ role: 'USER' });
    projectService.getProjects.mockReturnValue(of({ totalElements: 0 }));

    const result = await firstValueFrom(runGuard(route()) as ReturnType<typeof of>);

    expect(result).toBe(dashboardTree);
  });

  it('checks the requested project when guarding detail or member screens', async () => {
    authService.getCurrentUser.mockReturnValue({ role: 'USER' });
    projectService.getProjectById.mockReturnValue(of({ id: 'project-1' }));

    const result = await firstValueFrom(
      runGuard(route({}, { projectId: 'project-1' })) as ReturnType<typeof of>,
    );

    expect(result).toBe(true);
    expect(projectService.getProjectById).toHaveBeenCalledWith('project-1');
  });

  it('redirects when the project detail API denies access', async () => {
    authService.getCurrentUser.mockReturnValue({ role: 'USER' });
    projectService.getProjectById.mockReturnValue(throwError(() => new Error('Forbidden')));

    const result = await firstValueFrom(
      runGuard(route({ id: 'project-2' })) as ReturnType<typeof of>,
    );

    expect(result).toBe(dashboardTree);
  });

  function runGuard(snapshot: ActivatedRouteSnapshot) {
    return TestBed.runInInjectionContext(() => projectViewGuard(snapshot, null!));
  }

  function route(
    params: Record<string, string> = {},
    queryParams: Record<string, string> = {},
  ): ActivatedRouteSnapshot {
    return {
      paramMap: convertToParamMap(params),
      queryParamMap: convertToParamMap(queryParams),
    } as ActivatedRouteSnapshot;
  }
});
