import { projectViewGuard } from '../../core/guards/project-view-guard';
import { MEMBERS_ROUTES } from './members.routes';

describe('MEMBERS_ROUTES', () => {
  it.each(['', 'new'])('protects the %s route with projectViewGuard', path => {
    const route = MEMBERS_ROUTES.find(candidate => candidate.path === path);

    expect(route?.canActivate).toContain(projectViewGuard);
  });
});
