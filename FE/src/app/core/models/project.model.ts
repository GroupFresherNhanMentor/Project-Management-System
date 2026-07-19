import { ProjectStatus } from './api.model';

export interface ProjectDto {
  id: string;
  projectCode: string;
  projectName: string;
  description: string | null;
  startDate: string;
  endDate: string;
  status: ProjectStatus;
}

export interface CreateProjectRequest {
  projectCode: string;
  projectName: string;
  description?: string;
  startDate: string;
  endDate: string;
  status: ProjectStatus;
}

export interface UpdateProjectRequest {
  projectName: string;
  description?: string;
  startDate: string;
  endDate: string;
  status: ProjectStatus;
}

export interface ProjectListParams {
  keyword?: string;
  status?: ProjectStatus;
  page?: number;
  size?: number;
}
