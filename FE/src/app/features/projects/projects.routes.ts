import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards/admin-guard';
import { projectViewGuard } from '../../core/guards/project-view-guard';

export const PROJECTS_ROUTES: Routes = [
  {
    path: '',
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
    path: ':id',
    loadComponent: () =>
      import('./pages/project-layout/project-layout').then(m => m.ProjectLayout),
    data: { title: 'Project' },
    children: [
      {
        path: '',
        redirectTo: 'board',
        pathMatch: 'full',
      },
      {
        path: 'board',
        loadChildren: () => import('../board/board.routes').then(m => m.BOARD_ROUTES),
      },
      {
        path: 'backlog',
        loadChildren: () => import('../backlog/backlog.routes').then(m => m.BACKLOG_ROUTES),
      },
      {
        path: 'sprints',
        loadChildren: () => import('../sprints/sprints.routes').then(m => m.SPRINTS_ROUTES),
      },
      {
        path: 'members',
        loadChildren: () => import('../members/members.routes').then(m => m.MEMBERS_ROUTES),
      },
      {
        path: 'worklog',
        loadChildren: () => import('../worklog/worklog.routes').then(m => m.WORKLOG_ROUTES),
      },
    ],
  },
  {
    path: ':id/edit',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./pages/project-edit/project-edit').then(m => m.ProjectEdit),
    data: { title: 'Edit Project' },
  },
];
