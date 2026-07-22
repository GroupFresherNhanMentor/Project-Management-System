import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../services/auth';
import { ProjectContextService } from '../services/project-context';
import { ProjectService } from '../services/project';

/** Allows project management screens only to administrators or active project PMs. */
export const projectViewGuard: CanActivateFn = route => {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) return true;

  const authService = inject(AuthService);
  if (authService.getCurrentUser()?.role === 'ADMIN') return true;

  const router = inject(Router);
  const denied = router.createUrlTree(['/dashboard']);
  const projectService = inject(ProjectService);
  const projectContext = inject(ProjectContextService);
  const projectId = route.paramMap.get('id')
    ?? route.queryParamMap.get('projectId')
    ?? projectContext.selectedProjectId();

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
