import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { form, validateStandardSchema, FormField, FormRoot } from '@angular/forms/signals';
import { z } from 'zod';

import { AuthService } from '../../../../core/services/auth';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Please enter a valid email.'),
  password: z.string().min(6, 'Password must be at least 6 characters.'),
});

type LoginModel = z.infer<typeof loginSchema>;

@Component({
  selector: 'app-login',
  imports: [FormField, FormRoot],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly errorMessage = signal('');
  readonly isSubmitting = signal(false);

  readonly model = signal<LoginModel>({ email: '', password: '' });
  readonly loginForm = form(
    this.model,
    (f) => {
      validateStandardSchema(f, loginSchema);
    },
    {
      submission: {
        action: async () => {
          this.isSubmitting.set(true);
          this.errorMessage.set('');
          try {
            await firstValueFrom(this.authService.login(this.model()));
            void this.router.navigateByUrl('/products');
            return null;
          } catch {
            this.errorMessage.set('Login failed. Please check your credentials.');
            return null;
          } finally {
            this.isSubmitting.set(false);
          }
        },
        onInvalid: () => {
          this.isSubmitting.set(false);
        },
      },
    },
  );
}
