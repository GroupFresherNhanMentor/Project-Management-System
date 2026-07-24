export interface ApiResponse<T> {
  data: T;
  message: string | null;
  isSuccess: boolean;
}

export interface PageResponse<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

export interface ErrorResponse {
  success: false;
  errorCode: string;
  message: string;
  details: string[] | null;
}

export interface PageParams {
  page?: number;
  size?: number;
}

export type SystemRole = 'ADMIN' | 'USER';
export type UserStatus = 'ACTIVE' | 'LOCKED';
export type ProjectStatus = 'PLANNING' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED';
export type ProjectRole = 'PM' | 'DEV' | 'TESTER';
export type ProjectMemberStatus = 'ACTIVE' | 'INACTIVE';
export type SprintStatus = 'PLANNED' | 'ACTIVE' | 'CLOSED';
export type TaskType = 'STORY' | 'TASK' | 'BUG';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'TESTING' | 'DONE';
export type ActivityAction =
  | 'TASK_CREATED'
  | 'STATUS_CHANGED'
  | 'PRIORITY_CHANGED'
  | 'ASSIGNEE_CHANGED'
  | 'COMMENT_ADDED';
