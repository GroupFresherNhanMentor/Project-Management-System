import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth';

export const adminGuard: CanActivateFn = () => {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) return true;

  const user = inject(AuthService).getCurrentUser();
  return user?.role === 'ADMIN' ? true : inject(Router).createUrlTree(['/dashboard']);
};
