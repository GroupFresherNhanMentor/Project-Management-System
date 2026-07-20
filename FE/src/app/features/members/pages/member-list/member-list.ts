import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { InitialsPipe } from '../../../../shared/pipes/initials.pipe';

const MOCK_MEMBERS: ProjectMemberDto[] = [
  { id: 'm1', projectId: 'p1', userId: 'u2', userFullName: 'Lena Pham',   projectRole: 'PM',     status: 'ACTIVE' },
  { id: 'm2', projectId: 'p1', userId: 'u3', userFullName: 'Huy Tran',    projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm3', projectId: 'p1', userId: 'u4', userFullName: 'Mai Le',      projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm4', projectId: 'p1', userId: 'u5', userFullName: 'Khoa Nguyen', projectRole: 'TESTER', status: 'ACTIVE' },
];

@Component({
  selector: 'app-member-list',
  imports: [InitialsPipe],
  templateUrl: './member-list.html',
})
export class MemberList {
  private readonly router = inject(Router);

  readonly members = signal<ProjectMemberDto[]>(MOCK_MEMBERS);

  newMember(): void { void this.router.navigate(['/members/new']); }

  removeMember(member: ProjectMemberDto): void {
    this.members.update(list => list.filter(m => m.id !== member.id));
  }

  statusBadge(s: string): string {
    return s === 'ACTIVE' ? 'badge badge-active' : 'badge badge-locked';
  }
}
