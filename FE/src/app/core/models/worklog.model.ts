import { PageParams } from './api.model';

export interface WorklogDto {
  id: string;
  taskId: string;
  workDate: string;
  hour: number;
  description: string | null;
  createdBy: string;
}

export interface CreateWorklogRequest {
  workDate: string;
  hour: number;
  description?: string;
}

export interface UpdateWorklogRequest {
  workDate: string;
  hour: number;
  description?: string;
}

export interface WorklogReportItem {
  userId: string;
  userName: string;
  totalHours: number;
  numberOfTasks: number;
}

export interface WorklogReportParams extends PageParams {
  project?: string;
  user?: string;
  fromDate?: string;
  toDate?: string;
}
