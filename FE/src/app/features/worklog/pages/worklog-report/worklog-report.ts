import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { WorklogReportItem } from '../../../../core/models/worklog.model';
import { ProjectMemberDto } from '../../../../core/models/project-member.model';
import { WorklogService } from '../../../../core/services/worklog';
import { ProjectService } from '../../../../core/services/project';

@Component({
  selector: 'app-worklog-report',
  imports: [FormsModule],
  templateUrl: './worklog-report.html',
})
export class WorklogReport implements OnInit {
  private readonly router         = inject(Router);
  private readonly route          = inject(ActivatedRoute);
  private readonly worklogService = inject(WorklogService);
  private readonly projectService = inject(ProjectService);
  private readonly platformId     = inject(PLATFORM_ID);

  private readonly projectId = this.resolveProjectId();

  readonly items   = signal<WorklogReportItem[]>([]);
  readonly members = signal<ProjectMemberDto[]>([]);
  readonly loading = signal(false);
  readonly error   = signal<string | null>(null);

  userId   = '';
  fromDate = '';
  toDate   = '';
  page     = 0;
  size     = 20;
  totalPages    = 0;
  totalElements = 0;

  get totalPagesDisplay(): number { return this.totalPages || 1; }

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.projectService.getMembers(this.projectId, 0, 100).subscribe({
      next: res => this.members.set(res.items),
    });
    this.load();
  }

  load(): void {
    if (!this.projectId) return;
    this.loading.set(true);
    this.error.set(null);
    this.worklogService.getReport({
      project:  this.projectId,
      ...(this.userId   && { user:     this.userId }),
      ...(this.fromDate && { fromDate: this.fromDate }),
      ...(this.toDate   && { toDate:   this.toDate }),
      page: this.page,
      size: this.size,
    }).subscribe({
      next: res => {
        this.items.set(res.items);
        this.totalPages    = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load worklog report.');
        this.loading.set(false);
      },
    });
  }

  private resolveProjectId(): string {
    let r: ActivatedRoute | null = this.route;
    while (r) {
      const id = r.snapshot.paramMap.get('id');
      if (id) return id;
      r = r.parent;
    }
    return '';
  }

  prevPage(): void { if (this.page > 0)                   { this.page--; this.load(); } }
  nextPage(): void { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }

  openDetail(item: WorklogReportItem): void {
    void this.router.navigate([item.userId], { relativeTo: this.route });
  }
}
