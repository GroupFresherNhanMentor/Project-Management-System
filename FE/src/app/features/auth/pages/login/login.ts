import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { form, validateStandardSchema, FormField, FormRoot } from '@angular/forms/signals';
import { z } from 'zod';

import { AuthService } from '../../../../core/services/auth';

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

type LoginModel = z.infer<typeof loginSchema>;

@Component({
  selector: 'app-login',
  imports: [FormField, FormRoot],
  templateUrl: './login.html',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly errorMessage = signal('');
  readonly isSubmitting = signal(false);

  readonly model = signal<LoginModel>({ username: '', password: '' });
  readonly loginForm = form(
    this.model,
    f => { validateStandardSchema(f, loginSchema); },
    {
      submission: {
        action: async () => {
          this.isSubmitting.set(true);
          this.errorMessage.set('');
          try {
            await firstValueFrom(this.authService.login(this.model()));
            void this.router.navigateByUrl('/dashboard');
            return null;
          } catch {
            this.errorMessage.set('Invalid username or password.');
            return null;
          } finally {
            this.isSubmitting.set(false);
          }
        },
        onInvalid: () => { this.isSubmitting.set(false); },
      },
    },
  );
}
