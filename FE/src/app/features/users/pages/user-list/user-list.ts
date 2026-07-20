import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../../core/services/toast';
import type { UserDto } from '../../../../core/models/user.model';
import type { SystemRole, UserStatus } from '../../../../core/models/api.model';

const MOCK_USERS: UserDto[] = [
  { id: 'u1', employeeId: 'EMP001', username: 'admin',     fullName: 'Admin User',   email: 'admin@waypoint.io',   role: 'ADMIN', status: 'ACTIVE' },
  { id: 'u2', employeeId: 'EMP002', username: 'lena.pham', fullName: 'Lena Pham',    email: 'lena@waypoint.io',    role: 'USER',  status: 'ACTIVE' },
  { id: 'u3', employeeId: 'EMP003', username: 'huy.tran',  fullName: 'Huy Tran',     email: 'huy@waypoint.io',     role: 'USER',  status: 'ACTIVE' },
  { id: 'u4', employeeId: 'EMP004', username: 'mai.le',    fullName: 'Mai Le',       email: 'mai@waypoint.io',     role: 'USER',  status: 'ACTIVE' },
  { id: 'u5', employeeId: 'EMP005', username: 'khoa.ng',   fullName: 'Khoa Nguyen',  email: 'khoa@waypoint.io',    role: 'USER',  status: 'ACTIVE' },
  { id: 'u6', employeeId: 'EMP006', username: 'linh.dao',  fullName: 'Linh Dao',     email: 'linh@waypoint.io',    role: 'USER',  status: 'LOCKED' },
];

@Component({
  selector: 'app-user-list',
  imports: [FormsModule, RouterLink],
  templateUrl: './user-list.html',
})
export class UserList {
  private readonly toast = inject(ToastService);

  private readonly source = signal<UserDto[]>(MOCK_USERS);

  keyword = '';
  page = 0;
  readonly size = 10;

  editTarget: UserDto | null = null;
  editFullName = '';
  editEmail = '';
  editRole: SystemRole = 'USER';

  readonly roles: SystemRole[] = ['ADMIN', 'USER'];

  get filtered(): UserDto[] {
    const kw = this.keyword.toLowerCase().trim();
    if (!kw) return this.source();
    return this.source().filter(u =>
      u.fullName.toLowerCase().includes(kw) ||
      u.username.toLowerCase().includes(kw) ||
      u.email.toLowerCase().includes(kw) ||
      u.employeeId.toLowerCase().includes(kw)
    );
  }

  get totalPages(): number {
    return Math.ceil(this.filtered.length / this.size) || 1;
  }

  get paged(): UserDto[] {
    return this.filtered.slice(this.page * this.size, (this.page + 1) * this.size);
  }

  get showingFrom(): number { return this.filtered.length ? this.page * this.size + 1 : 0; }
  get showingTo(): number { return Math.min((this.page + 1) * this.size, this.filtered.length); }

  onSearch(): void { this.page = 0; }

  prevPage(): void { if (this.page > 0) this.page--; }
  nextPage(): void { if (this.page < this.totalPages - 1) this.page++; }

  openEdit(u: UserDto): void {
    this.editTarget = u;
    this.editFullName = u.fullName;
    this.editEmail = u.email;
    this.editRole = u.role;
  }

  saveEdit(): void {
    if (!this.editTarget) return;
    this.source.update(list => list.map(u =>
      u.id === this.editTarget!.id
        ? { ...u, fullName: this.editFullName, email: this.editEmail, role: this.editRole }
        : u
    ));
    this.toast.success('User updated.');
    this.editTarget = null;
  }

  toggleLock(u: UserDto): void {
    const newStatus: UserStatus = u.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    this.source.update(list => list.map(x => x.id === u.id ? { ...x, status: newStatus } : x));
    this.toast.success(newStatus === 'LOCKED' ? 'User locked.' : 'User unlocked.');
  }
}
