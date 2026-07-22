export const API = {
  auth: {
    login: '/api/auth/login',
    refresh: '/api/auth/refresh',
    logout: '/api/auth/logout',
  },
  users: {
    base: '/api/users',
    me: '/api/users/me',
    byId: (id: string) => `/api/users/${id}`,
    lock: (id: string) => `/api/users/${id}/lock`,
    resetPassword: (id: string) => `/api/users/${id}/reset-password`,
  },
  projects: {
    base: '/api/projects',
    byId: (id: string) => `/api/projects/${id}`,
    members: (projectId: string) => `/api/projects/${projectId}/members`,
    memberCandidates: (projectId: string) => `/api/projects/${projectId}/members/candidates`,
    memberById: (projectId: string, memberId: string) => `/api/projects/${projectId}/members/${memberId}`,
    sprints: (projectId: string) => `/api/projects/${projectId}/sprints`,
  },
  sprints: {
    status: (id: string) => `/api/sprints/${id}/status`,
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
