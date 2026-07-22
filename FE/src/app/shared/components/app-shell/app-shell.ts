import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';

import { AuthService } from '../../../core/services/auth';
import { InitialsPipe } from '../../pipes/initials.pipe';

@Component({
  selector: 'app-app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, InitialsPipe],
  templateUrl: './app-shell.html',
})
export class AppShell {
  private readonly authService    = inject(AuthService);
  private readonly router         = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

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

  readonly roleLabel = this.isAdmin ? 'Administrator' : 'Member';

  logout(): void { this.authService.logout(); }
}
