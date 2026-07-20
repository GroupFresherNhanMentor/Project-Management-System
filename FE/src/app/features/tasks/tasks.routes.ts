import { Routes } from '@angular/router';

export const TASKS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: '/backlog',
    pathMatch: 'full',
  },
  {
    path: 'new',
    loadComponent: () => import('./pages/task-new/task-new').then(m => m.TaskNew),
    data: { title: 'New Task' },
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/task-detail/task-detail').then(m => m.TaskDetail),
    data: { title: 'Task Detail' },
  },
];
