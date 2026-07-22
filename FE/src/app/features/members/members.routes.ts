import { Routes } from '@angular/router';
import { projectViewGuard } from '../../core/guards/project-view-guard';

export const MEMBERS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [projectViewGuard],
    loadComponent: () => import('./pages/member-list/member-list').then(m => m.MemberList),
    data: { title: 'Members' },
  },
  {
    path: 'new',
    canActivate: [projectViewGuard],
    loadComponent: () => import('./pages/member-new/member-new').then(m => m.MemberNew),
    data: { title: 'Add Member' },
  },
];
