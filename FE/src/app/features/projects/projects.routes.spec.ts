import { adminGuard } from '../../core/guards/admin-guard';
import { PROJECTS_ROUTES } from './projects.routes';

describe('PROJECTS_ROUTES', () => {
  it.each(['new', ':id/edit'])('protects the %s route with adminGuard', path => {
    const route = PROJECTS_ROUTES.find(candidate => candidate.path === path);

    expect(route?.canActivate).toContain(adminGuard);
  });

  it.each(['', ':id'])('keeps the %s route available to authenticated project users', path => {
    const route = PROJECTS_ROUTES.find(candidate => candidate.path === path);

    expect(route?.canActivate).toBeUndefined();
  });
});
