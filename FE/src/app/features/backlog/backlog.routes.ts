import { Routes } from '@angular/router';

export const BACKLOG_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/backlog/backlog').then(m => m.Backlog),
    data: { title: 'Backlog' },
  },
];
