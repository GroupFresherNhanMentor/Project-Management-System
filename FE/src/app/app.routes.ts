import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
	{
		path: 'login',
		loadComponent: () =>
			import('./features/auth/pages/login/login').then((m) => m.Login),
	},
	{
		path: 'products',
		canActivate: [authGuard],
		loadChildren: () =>
			import('./features/products/products.routes').then((m) => m.PRODUCTS_ROUTES),
	},
	{
		path: 'dashboard',
		canActivate: [authGuard],
		loadChildren: () =>
			import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
	},
	{
		path: '',
		pathMatch: 'full',
		redirectTo: 'products',
	},
	{
		path: '**',
		redirectTo: 'products',
	},
];
