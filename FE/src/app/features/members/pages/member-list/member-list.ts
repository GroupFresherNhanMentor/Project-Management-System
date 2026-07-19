import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { ProjectRole } from '../../../../core/models/api.model';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

const MOCK_MEMBERS: ProjectMemberDto[] = [
  { id: 'm1', projectId: 'p1', userId: 'u2', userFullName: 'Lena Pham',   projectRole: 'PM',     status: 'ACTIVE' },
  { id: 'm2', projectId: 'p1', userId: 'u3', userFullName: 'Huy Tran',    projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm3', projectId: 'p1', userId: 'u4', userFullName: 'Mai Le',      projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm4', projectId: 'p1', userId: 'u5', userFullName: 'Khoa Nguyen', projectRole: 'TESTER', status: 'ACTIVE' },
];

@Component({
  selector: 'app-member-list',
  imports: [FormsModule, InitialsPipe],
  templateUrl: './member-list.html',
})
export class MemberList {
  readonly members  = signal<ProjectMemberDto[]>(MOCK_MEMBERS);
  readonly allUsers = signal([
    { id: 'u6', fullName: 'New User' },
  ]);

  showAddForm = false;
  newUserId   = '';
  newRole: ProjectRole = 'DEV';
  readonly roles: ProjectRole[] = ['PM', 'DEV', 'TESTER'];

  addMember(): void { this.showAddForm = false; this.newUserId = ''; }

  removeMember(member: ProjectMemberDto): void {
    this.members.update(list => list.filter(m => m.id !== member.id));
  }

  statusBadge(s: string): string {
    return s === 'ACTIVE' ? 'badge badge-active' : 'badge badge-locked';
  }
}
