import { Routes } from '@angular/router';

export const BOARD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/board/board').then(m => m.Board),
    data: { title: 'Board' },
  },
];
