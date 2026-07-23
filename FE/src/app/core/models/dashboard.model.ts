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
  lockedUsers: number;
  totalProjects: number;
  activeProjects: number;
  taskDistribution: {
    STORY: number;
    TASK: number;
    BUG: number;
  };
  roleBreakdown: {
    PM: number;
    DEV: number;
    TESTER: number;
  };
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
  status: string;       // Nhận tên trạng thái động (statusName)
  statusColor: string;  // Bắt buộc phải có để render giao diện
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
