import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { inject } from '@angular/core';
import { WorklogReportItem } from '../../../../core/models/worklog.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';

const MOCK_MEMBERS: ProjectMemberDto[] = [
  { id: 'm1', projectId: 'p1', userId: 'u2', userFullName: 'Lena Pham',   projectRole: 'PM',     status: 'ACTIVE' },
  { id: 'm2', projectId: 'p1', userId: 'u3', userFullName: 'Huy Tran',    projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm3', projectId: 'p1', userId: 'u4', userFullName: 'Mai Le',      projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm4', projectId: 'p1', userId: 'u5', userFullName: 'Khoa Nguyen', projectRole: 'TESTER', status: 'ACTIVE' },
];

const MOCK_REPORT: WorklogReportItem[] = [
  { userId: 'u3', userName: 'Huy Tran',    totalHours: 18, numberOfTasks: 3 },
  { userId: 'u4', userName: 'Mai Le',      totalHours: 12, numberOfTasks: 2 },
  { userId: 'u5', userName: 'Khoa Nguyen', totalHours:  6, numberOfTasks: 1 },
  { userId: 'u2', userName: 'Lena Pham',   totalHours:  4, numberOfTasks: 1 },
];

@Component({
  selector: 'app-worklog-report',
  imports: [FormsModule],
  templateUrl: './worklog-report.html',
})
export class WorklogReport {
  private readonly router = inject(Router);

  readonly items   = signal<WorklogReportItem[]>(MOCK_REPORT);
  readonly members = signal<ProjectMemberDto[]>(MOCK_MEMBERS);

  userId   = '';
  fromDate = '';
  toDate   = '';

  load(): void {
    const filtered = MOCK_REPORT.filter(i =>
      (!this.userId   || i.userId === this.userId),
    );
    this.items.set(filtered);
  }

  openDetail(item: WorklogReportItem): void {
    void this.router.navigate(['/worklog', item.userId], {
      state: { userName: item.userName, totalHours: item.totalHours },
    });
  }
}
