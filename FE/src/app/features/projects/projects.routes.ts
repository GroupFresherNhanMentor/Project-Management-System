import { Routes } from '@angular/router';

export const PROJECTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/project-list/project-list').then(m => m.ProjectList),
    data: { title: 'Projects' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./pages/project-new/project-new').then(m => m.ProjectNew),
    data: { title: 'New Project' },
  },
];
