import { Component, inject, signal, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { UserService } from '../../../../core/services/user';
import { AuthService } from '../../../../core/services/auth';
import { ToastService } from '../../../../core/services/toast';

@Component({
  selector: 'app-profile',
  imports: [FormsModule],
  templateUrl: './profile.html',
})
export class Profile {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly user = signal(this.authService.getCurrentUser());
  private browser: boolean;

  fullName = this.user()?.fullName ?? '';
  email = this.user()?.email ?? '';
  saving = signal(false);

  showChangePassword = signal(false);

  oldPassword = '';
  newPassword = '';
  confirmPassword = '';
  changingPass = signal(false);

  constructor() {
    this.browser = isPlatformBrowser(this.platformId);
  }

  saveProfile(): void {
    this.saving.set(true);
    this.userService.updateCurrentUser({ fullName: this.fullName, email: this.email })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (updated) => {
          if (this.browser) {
            globalThis.localStorage.setItem('pms_user', JSON.stringify(updated));
          }
          this.user.set(updated);
          this.toast.success('Profile updated.');
        },
        error: (err: HttpErrorResponse) => {
          this.toast.error(err.error?.message ?? 'Failed to update profile.');
        },
      });
  }

  openChangePassword(): void {
    this.oldPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.showChangePassword.set(true);
  }

  closeChangePassword(): void {
    this.oldPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.showChangePassword.set(false);
  }

  doChangePassword(): void {
    if (!this.oldPassword || !this.newPassword || !this.confirmPassword) {
      this.toast.error('All password fields are required.');
      return;
    }
    if (this.newPassword.length < 6) {
      this.toast.error('Password must be at least 6 characters.');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.toast.error('New passwords do not match.');
      return;
    }
    this.changingPass.set(true);
    this.userService.changePassword({ oldPassword: this.oldPassword, newPassword: this.newPassword })
      .pipe(finalize(() => this.changingPass.set(false)))
      .subscribe({
        next: () => {
          this.toast.success('Password changed successfully.');
          this.closeChangePassword();
        },
        error: (err: HttpErrorResponse) => {
          this.toast.error(err.error?.message ?? 'Failed to change password.');
        },
      });
  }
}
