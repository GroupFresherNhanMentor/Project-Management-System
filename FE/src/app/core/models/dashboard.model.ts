export interface DashboardPersonalResponse {
  myOpenTasks: number;
  myCompletedTasks: number;
  myOverdueTasks: number;
  totalLoggedHours: number;
}

export interface SprintProgressDto {
  sprintId: string;
  sprintName: string;
  totalTasks: number;
  doneTasks: number;
  percentComplete: number;
}

export interface DashboardProjectResponse {
  totalTasks: number;
  taskByStatus: Record<string, number>;
  taskByPriority: Record<string, number>;
  totalLoggedHours: number;
  sprintProgress: SprintProgressDto | null;
}
