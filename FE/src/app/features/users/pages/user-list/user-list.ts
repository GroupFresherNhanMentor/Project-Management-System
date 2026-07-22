import { Component, signal, inject, PLATFORM_ID, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { finalize } from 'rxjs';

import { ToastService } from '../../../../core/services/toast';
import { UserService } from '../../../../core/services/user';
import type { UserDto, ResetPasswordResponse } from '../../../../core/models/user.model';
import type { SystemRole, UserStatus } from '../../../../core/models/api.model';

@Component({
  selector: 'app-user-list',
  imports: [FormsModule, RouterLink],
  templateUrl: './user-list.html',
})
export class UserList implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly userService = inject(UserService);
  private readonly toast = inject(ToastService);

  readonly users = signal<UserDto[]>([]);
  totalElements = 0;
  totalPages = 0;
  loading = false;

  keyword = '';
  page = 0;
  readonly size = 10;

  editTarget: UserDto | null = null;
  editFullName = '';
  editEmail = '';
  editRole: SystemRole = 'USER';

  readonly roles: SystemRole[] = ['ADMIN', 'USER'];

  resetTarget: UserDto | null = null;
  resetResult = signal<ResetPasswordResponse | null>(null);
  resetting = signal(false);

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.load();
    }
  }

  load(): void {
    this.loading = true;
    this.userService.getUsers({
      keyword: this.keyword || undefined,
      page: this.page,
      size: this.size,
    })
    .pipe(finalize(() => this.loading = false))
    .subscribe({
      next: (res) => {
        this.users.set(res.items);
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err.error?.message ?? 'Failed to load users.');
      },
    });
  }

  onSearch(): void { this.page = 0; this.load(); }
  prevPage(): void { if (this.page > 0) { this.page--; this.load(); } }
  nextPage(): void { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }

  openEdit(u: UserDto): void {
    this.editTarget = u;
    this.editFullName = u.fullName;
    this.editEmail = u.email;
    this.editRole = u.role;
  }

  saveEdit(): void {
    if (!this.editTarget) return;
    const target = this.editTarget;
    this.userService.updateUser(target.id, {
      fullName: this.editFullName,
      email: this.editEmail,
      role: this.editRole,
    })
    .subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
        this.toast.success('User updated.');
        this.editTarget = null;
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err.error?.message ?? 'Failed to update user.');
      },
    });
  }

  toggleLock(u: UserDto): void {
    const newStatus: UserStatus = u.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    this.userService.updateUserStatus(u.id, { status: newStatus })
    .subscribe({
      next: (updated) => {
        this.users.update(list => list.map(x => x.id === updated.id ? updated : x));
        this.toast.success(newStatus === 'LOCKED' ? 'User locked.' : 'User unlocked.');
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err.error?.message ?? 'Failed to update user status.');
      },
    });
  }

  confirmReset(u: UserDto): void {
    this.resetTarget = u;
  }

  cancelReset(): void {
    this.resetTarget = null;
  }

  doReset(): void {
    if (!this.resetTarget) return;
    const id = this.resetTarget.id;
    this.resetting.set(true);
    this.userService.resetPassword(id)
      .pipe(finalize(() => this.resetting.set(false)))
      .subscribe({
        next: (res) => {
          this.resetTarget = null;
          this.resetResult.set(res);
        },
        error: (err: HttpErrorResponse) => {
          this.toast.error(err.error?.message ?? 'Failed to reset password.');
          this.resetTarget = null;
        },
      });
  }

  closeResetResult(): void {
    this.resetResult.set(null);
  }

  copy(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.toast.success('Copied!');
    });
  }
}
