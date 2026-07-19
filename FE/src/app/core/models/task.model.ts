import { TaskType, TaskPriority, TaskStatus, PageParams } from './api.model';

export interface TaskDto {
  id: string;
  taskKey: string;
  projectId: string;
  sprintId: string | null;
  summary: string;
  description: string | null;
  taskType: TaskType;
  priority: TaskPriority;
  status: TaskStatus;
  assigneeId: string | null;
  assigneeName: string | null;
  reporterId: string;
  reporterName: string;
  storyPoint: number | null;
  estimateHour: number | null;
  dueDate: string | null;
  createdAt: string;
}

export interface CreateTaskRequest {
  projectId: string;
  sprintId?: string;
  summary: string;
  description?: string;
  taskType: TaskType;
  priority: TaskPriority;
  assigneeId?: string;
  reporterId: string;
  storyPoint?: number;
  estimateHour?: number;
  dueDate?: string;
}

export interface UpdateTaskRequest {
  status?: TaskStatus;
  description?: string;
  estimateHour?: number;
  dueDate?: string;
}

export interface AssignTaskRequest {
  assigneeId: string;
}

export interface TaskSearchParams extends PageParams {
  project?: string;
  sprint?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  assignee?: string;
  keyword?: string;
}
