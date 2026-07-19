import { Component, signal, inject } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { WorklogDto } from '../../../../core/models/worklog.model';

const MOCK_WORKLOGS: WorklogDto[] = [
  { id: 'wl1', taskId: 't1', workDate: '2026-07-14', hour: 4, description: 'Implemented new layout',    createdBy: 'u3' },
  { id: 'wl2', taskId: 't1', workDate: '2026-07-15', hour: 6, description: 'Connected step components', createdBy: 'u3' },
  { id: 'wl3', taskId: 't2', workDate: '2026-07-16', hour: 3, description: 'Stripe webhook handler',    createdBy: 'u3' },
  { id: 'wl4', taskId: 't5', workDate: '2026-07-17', hour: 5, description: 'Guest session flow',        createdBy: 'u3' },
];

@Component({
  selector: 'app-worklog-detail',
  imports: [RouterLink],
  templateUrl: './worklog-detail.html',
})
export class WorklogDetail {
  private readonly router = inject(Router);

  readonly worklogs = signal<WorklogDto[]>(MOCK_WORKLOGS);

  get userName(): string {
    const state = history.state as { userName?: string } | undefined;
    return state?.userName ?? 'Huy Tran';
  }

  get totalHours(): number {
    return this.worklogs().reduce((sum, w) => sum + w.hour, 0);
  }
}
