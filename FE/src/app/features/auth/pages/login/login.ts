import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { APP_CONSTANTS } from '../../../../configs/constants';

const MOCK_ACCOUNTS: Record<string, { role: 'ADMIN' | 'USER'; fullName: string }> = {
  admin:     { role: 'ADMIN', fullName: 'Admin User' },
  'lena.pham':  { role: 'USER',  fullName: 'Lena Pham'  },
  'huy.tran':   { role: 'USER',  fullName: 'Huy Tran'   },
};

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly router = inject(Router);

  username     = '';
  password     = '';
  errorMessage = signal('');
  isSubmitting = signal(false);

  submit(): void {
    if (!this.username || !this.password) { this.errorMessage.set('Username and password are required.'); return; }

    const account = MOCK_ACCOUNTS[this.username] ?? { role: 'USER' as const, fullName: this.username };

    const mockUser = { id: 'u1', username: this.username, fullName: account.fullName, email: `${this.username}@waypoint.io`, role: account.role, status: 'ACTIVE' };

    localStorage.setItem(APP_CONSTANTS.tokenKey, 'mock-token');
    localStorage.setItem(APP_CONSTANTS.userKey, JSON.stringify(mockUser));

    void this.router.navigateByUrl('/dashboard');
  }
}
