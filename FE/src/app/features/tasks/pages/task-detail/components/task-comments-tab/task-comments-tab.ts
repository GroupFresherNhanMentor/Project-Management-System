import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TaskService } from '../../../../../../core/services/task';
import { ToastService } from '../../../../../../core/services/toast';
import { TaskCommentDto } from '../../../../../../core/models/comment.model';
import { InitialsPipe } from '../../../../../../shared/pipes/initials.pipe';

@Component({
  selector: 'app-task-comments-tab',
  imports: [FormsModule, InitialsPipe],
  templateUrl: './task-comments-tab.html',
})
export class TaskCommentsTab implements OnInit {
  private readonly route       = inject(ActivatedRoute);
  private readonly taskService = inject(TaskService);
  private readonly toast       = inject(ToastService);
  private readonly platformId  = inject(PLATFORM_ID);

  private get taskId(): string {
    return this.route.parent?.snapshot.paramMap.get('id') ?? '';
  }

  readonly comments = signal<TaskCommentDto[]>([]);
  newComment = '';

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.taskService.getComments(this.taskId).subscribe({
      next: res => this.comments.set(res.items),
    });
  }

  postComment(): void {
    if (!this.newComment.trim()) return;
    this.taskService.addComment(this.taskId, { content: this.newComment }).subscribe({
      next: c => {
        this.comments.update(list => [...list, c]);
        this.newComment = '';
      },
      error: () => this.toast.error('Failed to post comment.'),
    });
  }
}
