export interface ApiResponse<T> {
  data: T;
  message: string;
  isSuccess: boolean;
  success: boolean;
}

export interface PersonalDashboardData {
  myOpenTasks: number;
  myCompletedTasks: number;
  myOverdueTasks: number;
  totalLoggedHours: number;
}

export interface SprintProgress {
  sprintId: string;
  sprintName: string;
  totalTasks: number;
  doneTasks: number;
  percentComplete: number;
}

export interface ProjectDashboardData {
  totalTasks: number;
  taskByStatus: {
    [key: string]: number;
    TODO: number;
    IN_PROGRESS: number;
    TESTING: number;
    DONE: number;
  };
  taskByPriority: {
    [key: string]: number;
    HIGH: number;
    MEDIUM: number;
    LOW: number;
    CRITICAL: number;
  };
  totalLoggedHours: number;
  sprintProgress: SprintProgress | null;
}

export interface AdminDashboardData {
  totalUsers: number;
  activeUsers: number;
  lockedUsers: number;
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  totalLoggedHours: number;
  projectByStatus: Record<string, number>;
  taskByType: Record<string, number>;
  userByRole: Record<string, number>;
}

export interface SystemActivity {
  id: string;
  userField: string;
  action: string;
  target: string;
  timestamp: string;
}

export interface DevTaskItem {
  id: string;
  taskKey: string;
  summary: string;
  status: 'TODO' | 'IN_PROGRESS' | 'TESTING' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  dueDate: string | null;
}

// Model dành cho API Task Search
export interface TaskSearchRequest {
  page: number;
  size: number;
  projectId?: string;
  sprintId?: string;
  status?: string;
  priority?: string;
  assigneeId?: string;
  keyword?: string;
}

export interface TaskSearchResponse {
  items: DevTaskItem[];
  total: number;
  page: number;
  size: number;
}
