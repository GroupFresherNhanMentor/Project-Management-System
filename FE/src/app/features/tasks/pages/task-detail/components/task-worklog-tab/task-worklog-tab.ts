import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TaskService } from '../../../../../../core/services/task';
import { AuthService } from '../../../../../../core/services/auth';
import { ToastService } from '../../../../../../core/services/toast';
import { WorklogDto } from '../../../../../../core/models/worklog.model';

@Component({
  selector: 'app-task-worklog-tab',
  imports: [FormsModule],
  templateUrl: './task-worklog-tab.html',
})
export class TaskWorklogTab implements OnInit {
  private readonly route       = inject(ActivatedRoute);
  private readonly taskService = inject(TaskService);
  private readonly authService = inject(AuthService);
  private readonly toast       = inject(ToastService);
  private readonly platformId  = inject(PLATFORM_ID);

  private get taskId(): string {
    return this.route.parent?.snapshot.paramMap.get('id') ?? '';
  }

  readonly worklogs = signal<WorklogDto[]>([]);
  newWorklogDate  = '';
  newWorklogHours: number | null = null;
  newWorklogDesc  = '';

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.taskService.getWorklogs(this.taskId).subscribe({
      next: res => this.worklogs.set(res.items),
    });
  }

  isMyWorklog(wl: WorklogDto): boolean {
    const user = this.authService.getCurrentUser();
    return !!user && wl.createdBy === user.fullName;
  }

  addWorklog(): void {
    if (!this.newWorklogDate || !this.newWorklogHours) {
      this.toast.error('Date and hours are required.');
      return;
    }
    this.taskService.addWorklog(this.taskId, {
      workDate:    this.newWorklogDate,
      hour:        this.newWorklogHours,
      description: this.newWorklogDesc || undefined,
    }).subscribe({
      next: w => {
        this.worklogs.update(list => [w, ...list]);
        this.newWorklogDate  = '';
        this.newWorklogHours = null;
        this.newWorklogDesc  = '';
        this.toast.success('Worklog added.');
      },
      error: () => this.toast.error('Failed to add worklog.'),
    });
  }

  deleteWorklog(wl: WorklogDto): void {
    this.taskService.deleteWorklog(wl.id).subscribe({
      next: () => {
        this.worklogs.update(list => list.filter(w => w.id !== wl.id));
        this.toast.success('Deleted.');
      },
      error: () => this.toast.error('Failed to delete worklog.'),
    });
  }
}
