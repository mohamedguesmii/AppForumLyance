export interface TaskResponse {
  status: 'success' | 'error';
  taskId?: string;
  message: string;
  details?: string;
}
