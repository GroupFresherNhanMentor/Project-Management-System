import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { AuthService } from '../../../../core/services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  username     = '';
  password     = '';
  errorMessage = signal('');
  isSubmitting = signal(false);

  submit(): void {
    if (!this.username || !this.password) {
      this.errorMessage.set('Username and password are required.');
      return;
    }

    this.errorMessage.set('');
    this.isSubmitting.set(true);

    this.authService.login({ username: this.username, password: this.password })
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => void this.router.navigateByUrl('/dashboard'),
        error: (err: HttpErrorResponse) => {
          this.errorMessage.set(err.error?.message ?? 'Login failed. Please check your credentials.');
        },
      });
  }
}
