import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskType, TaskPriority } from '../../../../core/models/api.model';
import { SprintDto } from '../../../../core/models/sprint.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';

const MOCK_SPRINTS: SprintDto[] = [
  { id: 's1', projectId: 'p1', sprintName: 'Sprint 12', goal: 'Ship checkout redesign', startDate: '2026-07-07', endDate: '2026-07-21', status: 'ACTIVE' },
  { id: 's3', projectId: 'p1', sprintName: 'Sprint 13', goal: 'TBD',                    startDate: '2026-07-22', endDate: '2026-08-04', status: 'PLANNED' },
];

const MOCK_MEMBERS: ProjectMemberDto[] = [
  { id: 'm1', projectId: 'p1', userId: 'u2', userFullName: 'Lena Pham',   projectRole: 'PM',     status: 'ACTIVE' },
  { id: 'm2', projectId: 'p1', userId: 'u3', userFullName: 'Huy Tran',    projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm3', projectId: 'p1', userId: 'u4', userFullName: 'Mai Le',      projectRole: 'DEV',    status: 'ACTIVE' },
  { id: 'm4', projectId: 'p1', userId: 'u5', userFullName: 'Khoa Nguyen', projectRole: 'TESTER', status: 'ACTIVE' },
];

@Component({
  selector: 'app-task-new',
  imports: [FormsModule],
  templateUrl: './task-new.html',
})
export class TaskNew {
  private readonly router = inject(Router);

  readonly sprints  = MOCK_SPRINTS;
  readonly members  = MOCK_MEMBERS;
  readonly taskTypes:   TaskType[]    = ['STORY', 'TASK', 'BUG'];
  readonly priorities:  TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  summary      = '';
  description  = '';
  taskType: TaskType    = 'TASK';
  priority: TaskPriority = 'MEDIUM';
  assigneeId   = '';
  sprintId     = '';
  storyPoint:  number | null = null;
  estimateHour: number | null = null;
  dueDate      = '';
  submitting   = false;

  submit(): void { void this.router.navigate(['/backlog']); }
  cancel(): void { void this.router.navigate(['/backlog']); }
}
