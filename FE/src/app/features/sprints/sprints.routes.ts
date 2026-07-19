import { Routes } from '@angular/router';

export const SPRINTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/sprint-list/sprint-list').then(m => m.SprintList),
    data: { title: 'Sprints' },
  },
  {
    path: 'new',
    loadComponent: () => import('./pages/sprint-new/sprint-new').then(m => m.SprintNew),
    data: { title: 'New Sprint' },
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/sprint-detail/sprint-detail').then(m => m.SprintDetail),
    data: { title: 'Sprint Detail' },
  },
];
