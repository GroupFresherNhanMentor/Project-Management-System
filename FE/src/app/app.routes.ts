import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';
import { AppShell } from './shared/components/app-shell/app-shell';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login').then(m => m.Login),
  },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES),
      },
      {
        path: 'board',
        loadChildren: () => import('./features/board/board.routes').then(m => m.BOARD_ROUTES),
      },
      {
        path: 'backlog',
        loadChildren: () => import('./features/backlog/backlog.routes').then(m => m.BACKLOG_ROUTES),
      },
      {
        path: 'sprints',
        loadChildren: () => import('./features/sprints/sprints.routes').then(m => m.SPRINTS_ROUTES),
      },
      {
        path: 'members',
        loadChildren: () => import('./features/members/members.routes').then(m => m.MEMBERS_ROUTES),
      },
      {
        path: 'worklog',
        loadChildren: () => import('./features/worklog/worklog.routes').then(m => m.WORKLOG_ROUTES),
      },
      {
        path: 'projects',
        loadChildren: () => import('./features/projects/projects.routes').then(m => m.PROJECTS_ROUTES),
      },
      {
        path: 'tasks',
        loadChildren: () => import('./features/tasks/tasks.routes').then(m => m.TASKS_ROUTES),
      },
      {
        path: 'users',
        canActivate: [adminGuard],
        loadChildren: () => import('./features/users/users.routes').then(m => m.USERS_ROUTES),
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '/dashboard' },
];
