import { ActivityAction } from './api.model';

export interface TaskActivityDto {
  id: string;
  taskId: string;
  userId: string;
  userName: string;
  action: ActivityAction;
  oldValue: string | null;
  newValue: string | null;
  message: string | null;
  createdTime: string;
}
