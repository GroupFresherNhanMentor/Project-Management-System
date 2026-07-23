import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { TaskService } from '../../../../../../core/services/task';
import { TaskActivityDto } from '../../../../../../core/models/activity.model';
import { InitialsPipe } from '../../../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-task-activity-tab',
  imports: [InitialsPipe],
  templateUrl: './task-activity-tab.html',
})
export class TaskActivityTab implements OnInit {
  private readonly route       = inject(ActivatedRoute);
  private readonly taskService = inject(TaskService);
  private readonly platformId  = inject(PLATFORM_ID);

  private get taskId(): string {
    return this.route.parent?.snapshot.paramMap.get('id') ?? '';
  }

  readonly activities = signal<TaskActivityDto[]>([]);

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.taskService.getActivities(this.taskId).subscribe({
      next: res => this.activities.set(res.items),
    });
  }

  activityLabel(a: TaskActivityDto): string {
    const who = a.userName ?? 'Someone';
    const msg = a.message ?? 'performed an action';
    return `${who} ${msg}`;
  }
}
