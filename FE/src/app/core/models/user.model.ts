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
  fullName: string;
  email: string;
  role: SystemRole;
}

export interface CreateUserResponse {
  user: UserDto;
  generatedPassword: string;
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

export interface UpdateCurrentUserRequest {
  fullName?: string;
  email?: string;
  password?: string;
}

export interface ResetPasswordResponse {
  userId: string;
  username: string;
  generatedPassword: string;
}
