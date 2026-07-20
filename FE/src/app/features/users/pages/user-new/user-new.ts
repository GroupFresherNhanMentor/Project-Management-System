import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { ToastService } from '../../../../core/services/toast';
import type { SystemRole } from '../../../../core/models/api.model';

@Component({
  selector: 'app-user-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './user-new.html',
})
export class UserNew {
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  employeeId = '';
  username = '';
  fullName = '';
  email = '';
  password = '';
  role: SystemRole = 'USER';
  submitting = signal(false);

  readonly roles: SystemRole[] = ['ADMIN', 'USER'];

  submit(): void {
    if (!this.username || !this.fullName || !this.email || !this.password) {
      this.toast.error('Username, full name, email and password are required.');
      return;
    }

    this.submitting.set(true);
    this.toast.success('User created successfully.');
    void this.router.navigate(['/users']);
  }
}
