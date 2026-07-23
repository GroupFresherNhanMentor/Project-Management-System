import { Routes } from '@angular/router';

export const TASKS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: '/backlog',
    pathMatch: 'full',
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/task-detail/task-detail').then(m => m.TaskDetail),
    data: { title: 'Task Detail' },
    children: [
      { path: '', redirectTo: 'details', pathMatch: 'full' },
      {
        path: 'details',
        loadComponent: () => import('./pages/task-detail/components/task-details-tab/task-details-tab').then(m => m.TaskDetailsTab),
      },
      {
        path: 'comments',
        loadComponent: () => import('./pages/task-detail/components/task-comments-tab/task-comments-tab').then(m => m.TaskCommentsTab),
      },
      {
        path: 'worklog',
        loadComponent: () => import('./pages/task-detail/components/task-worklog-tab/task-worklog-tab').then(m => m.TaskWorklogTab),
      },
      {
        path: 'activity',
        loadComponent: () => import('./pages/task-detail/components/task-activity-tab/task-activity-tab').then(m => m.TaskActivityTab),
      },
    ],
  },
];
