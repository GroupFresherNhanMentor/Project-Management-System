import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../../core/services/toast';
import { inject } from '@angular/core';
import { UserDto } from '../../../../core/models/user.model';
import { SystemRole } from '../../../../core/models/api.model';

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

  private readonly _users = signal<UserDto[]>(MOCK_USERS);

  keyword  = '';
  page     = 0;
  size     = 10;

  private filteredUsers(): UserDto[] {
    const kw = this.keyword.toLowerCase();
    return this._users().filter(u =>
      !kw || u.fullName.toLowerCase().includes(kw) || u.username.toLowerCase().includes(kw) || u.email.toLowerCase().includes(kw),
    );
  }
  get total(): number { return this.filteredUsers().length; }
  get totalPages(): number { return Math.ceil(this.total / this.size) || 1; }
  users(): UserDto[] {
    return this.filteredUsers().slice(this.page * this.size, (this.page + 1) * this.size);
  }
  load(): void { /* filtering is live via users() */ }

  showNewForm = false;
  newEmpId    = '';
  newUsername = '';
  newFullName = '';
  newEmail    = '';
  newRole: SystemRole = 'USER';

  editTarget: UserDto | null = null;
  editFullName = '';
  editEmail    = '';
  editRole: SystemRole = 'USER';

  readonly roles: SystemRole[] = ['ADMIN', 'USER'];

  prevPage(): void { if (this.page > 0) this.page--; }
  nextPage(): void { if (this.page < this.totalPages - 1) this.page++; }

  createUser(): void {
    if (!this.newUsername || !this.newFullName || !this.newEmail) { this.toast.error('Fill required fields.'); return; }
    const u: UserDto = {
      id: crypto.randomUUID(), employeeId: this.newEmpId,
      username: this.newUsername, fullName: this.newFullName,
      email: this.newEmail, role: this.newRole, status: 'ACTIVE',
    };
    this._users.update(list => [...list, u]);
    this.toast.success('User created.');
    this.showNewForm = false;
    this.resetNew();
  }

  openEdit(u: UserDto): void {
    this.editTarget   = u;
    this.editFullName = u.fullName;
    this.editEmail    = u.email;
    this.editRole     = u.role;
  }

  saveEdit(): void {
    if (!this.editTarget) return;
    this._users.update(list => list.map(u =>
      u.id === this.editTarget!.id
        ? { ...u, fullName: this.editFullName, email: this.editEmail, role: this.editRole }
        : u,
    ));
    this.toast.success('User updated.');
    this.editTarget = null;
  }

  toggleLock(u: UserDto): void {
    const newStatus = u.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    this._users.update(list => list.map(x => x.id === u.id ? { ...x, status: newStatus } : x));
    this.toast.success(newStatus === 'LOCKED' ? 'User locked.' : 'User unlocked.');
  }

  resetNew(): void {
    this.newEmpId = ''; this.newUsername = ''; this.newFullName = '';
    this.newEmail = ''; this.newRole = 'USER';
  }

  statusBadge(s: string): string {
    return s === 'ACTIVE' ? 'badge badge-active' : 'badge badge-locked';
  }
}
