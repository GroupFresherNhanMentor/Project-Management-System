import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../services/auth';
import { ProjectService } from '../services/project';

/** Allows project management screens only to administrators or active project PMs. */
export const projectViewGuard: CanActivateFn = route => {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) return true;

  const authService = inject(AuthService);
  if (authService.getCurrentUser()?.role === 'ADMIN') return true;

  const router = inject(Router);
  const denied = router.createUrlTree(['/dashboard']);
  const projectService = inject(ProjectService);

  // Walk up route tree to find :id param (handles /projects/:id/... screens)
  let projectId = route.paramMap.get('id');
  if (!projectId) {
    let r = route.parent;
    while (r) {
      projectId = r.paramMap.get('id');
      if (projectId) break;
      r = r.parent;
    }
  }
  // Fallback to query param
  if (!projectId) {
    projectId = route.queryParamMap.get('projectId');
  }

  const accessCheck = projectId
    ? projectService.getProjectById(projectId).pipe(map(() => true))
    : projectService.getProjects({ page: 0, size: 1 }).pipe(
        map(page => page.totalElements > 0),
      );

  return accessCheck.pipe(
    map(allowed => allowed ? true : denied),
    catchError(() => of(denied)),
  );
};
