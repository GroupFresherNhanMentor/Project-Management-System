import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorklogService } from '../../../../core/services/worklog';
import { WorklogReportItem, WorklogReportParams } from '../../../../core/models/worklog.model';
import { PageResponse } from '../../../../core/models/api.model';

@Component({
  selector: 'app-worklog-report',
  imports: [CommonModule],
  templateUrl: './worklog-report.html',
})
export class WorklogReport implements OnInit {
  private readonly worklogService = inject(WorklogService);

  readonly data = signal<PageResponse<WorklogReportItem> | null>(null);
  readonly params = signal<WorklogReportParams>({ page: 0, size: 20 });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.worklogService.getReport(this.params()).subscribe({
      next: res => this.data.set(res),
    });
  }
}
