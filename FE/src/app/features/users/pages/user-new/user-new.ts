import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { ToastService } from '../../../../core/services/toast';
import { SystemRole } from '../../../../core/models/api.model';

@Component({
  selector: 'app-user-new',
  imports: [FormsModule, RouterLink],
  templateUrl: './user-new.html',
})
export class UserNew {
  private readonly toast  = inject(ToastService);
  private readonly router = inject(Router);

  employeeId = '';
  username   = '';
  fullName   = '';
  email      = '';
  role: SystemRole = 'USER';
  submitting = false;

  readonly roles: SystemRole[] = ['ADMIN', 'USER'];

  submit(): void {
    if (!this.username || !this.fullName || !this.email) {
      this.toast.error('Username, full name and email are required.'); return;
    }
    this.submitting = true;
    this.toast.success('User created.');
    void this.router.navigate(['/users']);
  }
}
