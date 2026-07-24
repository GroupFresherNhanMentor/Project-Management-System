export interface TaskCommentDto {
  id: string;
  taskId: string;
  content: string;
  createdBy: string;
  createdByName: string;
  createdTime: string;
}

export interface CreateCommentRequest {
  content: string;
}

export interface UpdateCommentRequest {
  content: string;
}
