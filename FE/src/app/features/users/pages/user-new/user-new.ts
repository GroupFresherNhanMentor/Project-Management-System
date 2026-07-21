import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { ToastService } from '../../../../core/services/toast';
import { UserService } from '../../../../core/services/user';
import type { SystemRole } from '../../../../core/models/api.model';
import type { CreateUserResponse } from '../../../../core/models/user.model';

@Component({
  selector: 'app-user-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './user-new.html',
})
export class UserNew {
  private readonly userService = inject(UserService);
  private readonly toast = inject(ToastService);

  fullName = '';
  email = '';
  role: SystemRole = 'USER';
  submitting = signal(false);
  result = signal<CreateUserResponse | null>(null);

  readonly roles: SystemRole[] = ['ADMIN', 'USER'];

  submit(): void {
    if (!this.fullName || !this.email) {
      this.toast.error('Full name and email are required.');
      return;
    }

    this.submitting.set(true);

    this.userService.createUser({
      fullName: this.fullName,
      email: this.email,
      role: this.role,
    })
    .pipe(finalize(() => this.submitting.set(false)))
    .subscribe({
      next: (res) => {
        this.result.set(res);
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err.error?.message ?? 'Failed to create user.');
      },
    });
  }

  closeResult(): void {
    this.result.set(null);
    this.fullName = '';
    this.email = '';
    this.role = 'USER';
  }

  copy(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.toast.success('Copied!');
    });
  }
}
