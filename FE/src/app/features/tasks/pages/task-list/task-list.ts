import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../../../core/services/task';
import { TaskDto, TaskSearchParams } from '../../../../core/models/task.model';
import { PageResponse } from '../../../../core/models/api.model';

@Component({
  selector: 'app-task-list',
  imports: [CommonModule],
  templateUrl: './task-list.html',
})
export class TaskList implements OnInit {
  private readonly taskService = inject(TaskService);

  readonly data = signal<PageResponse<TaskDto> | null>(null);
  readonly params = signal<TaskSearchParams>({ page: 0, size: 20 });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.taskService.searchTasks(this.params()).subscribe({
      next: res => this.data.set(res),
    });
  }
}
