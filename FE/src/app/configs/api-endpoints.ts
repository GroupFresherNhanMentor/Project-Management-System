export const API = {
  auth: {
    login: '/api/auth/login',
    refresh: '/api/auth/refresh',
    logout: '/api/auth/logout',
  },
  users: {
    base: '/api/users',
    me: '/api/users/me',
    changePassword: '/api/users/me/change-password',
    byId: (id: string) => `/api/users/${id}`,
    lock: (id: string) => `/api/users/${id}/lock`,
    resetPassword: (id: string) => `/api/users/${id}/reset-password`,
  },
  projects: {
    base: '/api/projects',
    byId: (id: string) => `/api/projects/${id}`,
    members: (projectId: string) => `/api/projects/${projectId}/members`,
    currentMember: (projectId: string) => `/api/projects/${projectId}/members/me`,
    memberCandidates: (projectId: string) => `/api/projects/${projectId}/members/candidates`,
    memberById: (projectId: string, memberId: string) => `/api/projects/${projectId}/members/${memberId}`,
    sprints: (projectId: string) => `/api/projects/${projectId}/sprints`,
  },
  sprints: {
    byId: (projectId: string, id: string) => `/api/projects/${projectId}/sprints/${id}`,
    status: (projectId: string, id: string) => `/api/projects/${projectId}/sprints/${id}/status`,
  },
  tasks: {
    base: '/api/tasks',
    search: '/api/tasks/search',
    byId: (id: string) => `/api/tasks/${id}`,
    assign: (id: string) => `/api/tasks/${id}/assign`,
    comments: (taskId: string) => `/api/tasks/${taskId}/comments`,
    activities: (taskId: string) => `/api/tasks/${taskId}/activities`,
    worklogs: (taskId: string) => `/api/tasks/${taskId}/worklogs`,
  },
  taskStatuses: {
    base: (projectId: string) => `/api/projects/${projectId}/task-statuses`,
    byId: (projectId: string, id: string) => `/api/projects/${projectId}/task-statuses/${id}`,
  },
  taskWorkflow: {
    base: (projectId: string) => `/api/projects/${projectId}/task-workflow`,
    byId: (projectId: string, id: string) => `/api/projects/${projectId}/task-workflow/${id}`,
  },
  worklogs: {
    byId: (id: string) => `/api/worklogs/${id}`,
  },
  dashboard: {
    me: '/api/dashboard/me',
    project: (projectId: string) => `/api/dashboard/project/${projectId}`,
  },
  reports: {
    worklog: '/api/reports/worklog',
  },
};
