import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards/admin-guard';
import { projectViewGuard } from '../../core/guards/project-view-guard';

export const PROJECTS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [projectViewGuard],
    loadComponent: () =>
      import('./pages/project-list/project-list').then(m => m.ProjectList),
    data: { title: 'Projects' },
  },
  {
    path: 'new',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./pages/project-new/project-new').then(m => m.ProjectNew),
    data: { title: 'New Project' },
  },
  {
    path: ':id/edit',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./pages/project-edit/project-edit').then(m => m.ProjectEdit),
    data: { title: 'Edit Project' },
  },
  {
    path: ':id',
    canActivate: [projectViewGuard],
    loadComponent: () =>
      import('./pages/project-detail/project-detail').then(m => m.ProjectDetail),
    data: { title: 'Project Detail' },
  },
];
