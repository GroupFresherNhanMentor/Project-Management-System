import { adminGuard } from '../../core/guards/admin-guard';
import { projectViewGuard } from '../../core/guards/project-view-guard';
import { PROJECTS_ROUTES } from './projects.routes';

describe('PROJECTS_ROUTES', () => {
  it.each(['new', ':id/edit'])('protects the %s route with adminGuard', path => {
    const route = PROJECTS_ROUTES.find(candidate => candidate.path === path);

    expect(route?.canActivate).toContain(adminGuard);
  });

  it.each(['', ':id'])('protects the %s route with projectViewGuard', path => {
    const route = PROJECTS_ROUTES.find(candidate => candidate.path === path);

    expect(route?.canActivate).toContain(projectViewGuard);
  });
});
