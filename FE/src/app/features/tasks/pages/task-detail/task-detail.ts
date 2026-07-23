import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TaskService } from '../../../../core/services/task';
import { TaskDetailService } from './task-detail.service';

type Tab = 'details' | 'comments' | 'worklog' | 'activity';

@Component({
  selector: 'app-task-detail',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  providers: [TaskDetailService],
  templateUrl: './task-detail.html',
})
export class TaskDetail implements OnInit {
  private readonly route       = inject(ActivatedRoute);
  private readonly router      = inject(Router);
  private readonly location    = inject(Location);
  private readonly taskService = inject(TaskService);
  readonly taskSvc             = inject(TaskDetailService);
  private readonly platformId  = inject(PLATFORM_ID);

  private readonly taskId = this.route.snapshot.paramMap.get('id') ?? '';

  readonly loading = signal(true);
  readonly tabs: Tab[] = ['details', 'comments', 'worklog', 'activity'];

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.taskService.getTaskById(this.taskId).subscribe({
      next: t => {
        this.taskSvc.task.set(t);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  goBack(): void {
    const projectId = this.taskSvc.task()?.projectId;
    if (projectId) {
      void this.router.navigate(['/projects', projectId, 'backlog']);
    } else {
      this.location.back();
    }
  }

  typeLabel(t: string): string {
    return ({ STORY: 'Story', TASK: 'Task', BUG: 'Bug' } as Record<string, string>)[t] ?? t;
  }
}
