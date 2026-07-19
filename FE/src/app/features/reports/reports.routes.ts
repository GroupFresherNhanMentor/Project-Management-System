import { Routes } from '@angular/router';

export const REPORTS_ROUTES: Routes = [
  { path: '', redirectTo: '/worklog', pathMatch: 'full' },
  { path: 'worklog', redirectTo: '/worklog', pathMatch: 'full' },
];
