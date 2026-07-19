import { Component, inject, computed } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';

import { AuthService } from '../../../core/services/auth';
import { ProjectContextService } from '../../../core/services/project-context';
import { InitialsPipe } from '../../pipes/initials.pipe';

const MOCK_PROJECTS = [
  { id: 'p1', projectCode: 'WEB', projectName: 'Website Revamp',  description: null, startDate: '2026-07-01', endDate: '2026-09-30', status: 'ACTIVE'   as const },
  { id: 'p2', projectCode: 'MOB', projectName: 'Mobile App',       description: null, startDate: '2026-06-01', endDate: '2026-12-31', status: 'PLANNING' as const },
];

@Component({
  selector: 'app-app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, InitialsPipe],
  templateUrl: './app-shell.html',
})
export class AppShell {
  private readonly authService    = inject(AuthService);
  private readonly router         = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  readonly projectContext = inject(ProjectContextService);
  readonly currentUser   = this.authService.getCurrentUser();
  readonly isAdmin       = this.currentUser?.role === 'ADMIN';

  readonly todayDate = new Date().toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
  });

  readonly pageTitle = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let route = this.activatedRoute.snapshot;
        while (route.firstChild) route = route.firstChild;
        return (route.data['title'] as string | undefined) ?? 'Dashboard';
      }),
    ),
    { initialValue: 'Dashboard' },
  );

  readonly roleLabel = computed(() => {
    if (this.isAdmin) return 'Administrator';
    return this.projectContext.currentUserProjectRole() ?? 'PM';
  });

  constructor() {
    if (!this.isAdmin) {
      this.projectContext.setProjects(MOCK_PROJECTS);
      const roleMap: Record<string, 'PM' | 'DEV' | 'TESTER'> = {
        'lena.pham': 'PM',
        'huy.tran':  'DEV',
        'mai.le':    'DEV',
        'khoa.ng':   'TESTER',
      };
      const role = roleMap[this.currentUser?.username ?? ''] ?? 'PM';
      this.projectContext.setCurrentUserRole(role);
    }
  }

  onProjectChange(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    this.projectContext.selectProject(id);
  }

  logout(): void { this.authService.logout(); }
}
