import { Routes } from '@angular/router';

export const MEMBERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/member-list/member-list').then(m => m.MemberList),
    data: { title: 'Members' },
  },
];
