import { TaskType, TaskPriority, PageParams } from './api.model';

export interface TaskStatusDto {
  id: string;
  projectId: string;
  name: string;
  color: string;
  isInitial: boolean;
  isFinal: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TaskDto {
  id: string;
  taskKey: string;
  projectId: string;
  sprintId: string | null;
  summary: string;
  description: string | null;
  taskType: TaskType;
  priority: TaskPriority;
  statusId: string;
  statusName: string;
  statusColor: string;
  assigneeId: string | null;
  assigneeName: string | null;
  reporterId: string;
  reporterName: string;
  storyPoint: number | null;
  estimateHour: number | null;
  dueDate: string | null;
  createdAt: string;
}

export interface TaskWorkflowDto {
  id: string;
  fromStatusId: string;
  toStatusId: string;
  createdAt: string;
}

export interface CreateTaskStatusRequest {
  name: string;
  color?: string;
  isInitial?: boolean;
  isFinal?: boolean;
}

export interface CreateTaskWorkflowRequest {
  fromStatusId: string;
  toStatusId: string;
}

export interface CreateTaskRequest {
  taskKey: string;
  projectId: string;
  sprintId?: string;
  summary: string;
  description?: string;
  taskType: TaskType;
  priority: TaskPriority;
  taskStatusId: string;
  assigneeId?: string;
  reporterId: string;
  storyPoint?: number;
  estimateHour?: number;
  dueDate?: string;
}

export interface UpdateTaskRequest {
  statusId?: string;
  description?: string;
  estimateHour?: number;
  dueDate?: string;
}

export interface AssignTaskRequest {
  assigneeId: string | null; // null = unassign
}

export interface TaskSearchParams extends PageParams {
  projectId?: string;
  sprintId?: string;
  statusId?: string;
  priority?: TaskPriority;
  assigneeId?: string;
  keyword?: string;
}
