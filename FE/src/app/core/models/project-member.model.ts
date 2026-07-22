import { ProjectRole, ProjectMemberStatus } from './api.model';

export interface ProjectMemberDto {
  id: string;
  projectId: string;
  userId: string;
  employeeId?: string;
  userFullName: string;
  email?: string;
  projectRole: ProjectRole;
  status: ProjectMemberStatus;
  createdAt?: string;
}

export interface AddProjectMemberRequest {
  userId: string;
  projectRole: ProjectRole;
}

export interface ProjectMemberCandidateDto {
  id: string;
  employeeId: string;
  fullName: string;
  email: string;
}
