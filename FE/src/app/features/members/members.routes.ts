import { Routes } from '@angular/router';

export const MEMBERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/member-list/member-list').then(m => m.MemberList),
    data: { title: 'Members' },
  },
  {
    path: 'new',
    loadComponent: () => import('./pages/member-new/member-new').then(m => m.MemberNew),
    data: { title: 'Add Member' },
  },
];
