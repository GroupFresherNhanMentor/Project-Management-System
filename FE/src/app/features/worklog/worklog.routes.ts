import { Routes } from '@angular/router';

export const WORKLOG_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/worklog-report/worklog-report').then(m => m.WorklogReport),
    data: { title: 'Worklog Report' },
  },
  {
    path: ':userId',
    loadComponent: () => import('./pages/worklog-detail/worklog-detail').then(m => m.WorklogDetail),
    data: { title: 'Worklog Detail' },
  },
];
