import { Component, signal } from '@angular/core';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { SprintDto } from '../../../../core/models/sprint.model';

const MOCK_SPRINTS: SprintDto[] = [
  { id: 's1', projectId: 'p1', sprintName: 'Sprint 12', goal: 'Ship checkout redesign', startDate: '2026-07-07', endDate: '2026-07-21', status: 'ACTIVE'  },
  { id: 's2', projectId: 'p1', sprintName: 'Sprint 11', goal: 'Complete auth revamp',   startDate: '2026-06-23', endDate: '2026-07-06', status: 'CLOSED'  },
  { id: 's3', projectId: 'p1', sprintName: 'Sprint 13', goal: 'TBD',                    startDate: '2026-07-22', endDate: '2026-08-04', status: 'PLANNED' },
];

@Component({
  selector: 'app-sprint-list',
  templateUrl: './sprint-list.html',
})
export class SprintList {
  private readonly router = inject(Router);
  readonly sprints = signal(MOCK_SPRINTS);

  newSprint(): void { void this.router.navigate(['/sprints/new']); }

  viewSprint(sprint: SprintDto): void {
    void this.router.navigate(['/sprints', sprint.id], { state: { sprint } });
  }

  startSprint(sprint: SprintDto, event: Event): void {
    event.stopPropagation();
    this.sprints.update(list => list.map(s => s.id === sprint.id ? { ...s, status: 'ACTIVE' as const } : s));
  }

  closeSprint(sprint: SprintDto, event: Event): void {
    event.stopPropagation();
    this.sprints.update(list => list.map(s => s.id === sprint.id ? { ...s, status: 'CLOSED' as const } : s));
  }

  statusBadge(status: string): string {
    const m: Record<string, string> = { PLANNED: 'badge-planned', ACTIVE: 'badge-active', CLOSED: 'badge-closed' };
    return 'badge ' + (m[status] ?? '');
  }
}
