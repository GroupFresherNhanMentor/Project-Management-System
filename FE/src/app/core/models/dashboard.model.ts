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

export interface DashboardActivityItem {
  id: string;
  taskId: string;
  taskKey: string;
  userId: string;
  userName: string;
  action: 'TASK_CREATED' | 'STATUS_CHANGED' | 'PRIORITY_CHANGED' | 'ASSIGNEE_CHANGED' | 'COMMENT_ADDED' | 'COMMENT_DELETED';
  message: string;
  createdTime: string;
}

export interface DevTaskItem {
  id: string;
  taskKey: string;
  summary: string;
  status: string;
  statusColor?: string;
  statusId?: string | null;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  dueDate: string | null;
}
// Model dành cho API Task Search (Khớp params trên Swagger)
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

// Cập nhật cấu trúc Phân trang thực tế từ Swagger
export interface TaskSearchResponse {
  items: any[];
  totalElements: number; // Thay thế cho biến total cũ
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}
