import { SprintStatus } from './api.model';

export interface SprintDto {
  id: string;
  projectId: string;
  sprintName: string;
  goal: string | null;
  startDate: string;
  endDate: string;
  status: SprintStatus;
}

export interface CreateSprintRequest {
  sprintName: string;
  goal?: string;
  startDate: string;
  endDate: string;
}

export interface UpdateSprintStatusRequest {
  status: SprintStatus;
}

export interface UpdateSprintRequest {
  sprintName?: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
}
