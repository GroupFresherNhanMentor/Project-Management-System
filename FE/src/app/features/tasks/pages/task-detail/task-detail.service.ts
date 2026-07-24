import { Injectable, signal } from '@angular/core';
import { TaskDto } from '../../../../core/models/task.model';

@Injectable()
export class TaskDetailService {
  readonly task = signal<TaskDto | null>(null);
}
