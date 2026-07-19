import { Routes } from '@angular/router';

export const REPORTS_ROUTES: Routes = [
  {
    path: 'worklog',
    loadComponent: () =>
      import('./pages/worklog-report/worklog-report').then(m => m.WorklogReport),
  },
];
