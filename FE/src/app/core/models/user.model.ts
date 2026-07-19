import { SystemRole, UserStatus } from './api.model';

export interface UserDto {
  id: string;
  employeeId: string;
  username: string;
  fullName: string;
  email: string;
  role: SystemRole;
  status: UserStatus;
}

export interface CreateUserRequest {
  employeeId: string;
  username: string;
  password: string;
  fullName: string;
  email: string;
  role: SystemRole;
}

export interface UpdateUserRequest {
  fullName?: string;
  email?: string;
  role?: SystemRole;
}

export interface UpdateUserStatusRequest {
  status: UserStatus;
}

export interface UserListParams {
  keyword?: string;
  role?: SystemRole;
  status?: UserStatus;
  page?: number;
  size?: number;
}
