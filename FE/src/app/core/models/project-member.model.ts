import { ProjectRole, ProjectMemberStatus } from './api.model';

export interface ProjectMemberDto {
  id: string;
  projectId: string;
  userId: string;
  userFullName: string;
  projectRole: ProjectRole;
  status: ProjectMemberStatus;
}

export interface AddProjectMemberRequest {
  userId: string;
  projectRole: ProjectRole;
}
