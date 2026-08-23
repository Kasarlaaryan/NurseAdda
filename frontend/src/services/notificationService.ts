import apiClient from './apiClient';

export interface NotificationResponse {
  id: number;
  title: string;
  message: string;
  type: string;
  link: string;
  read: boolean;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const notificationService = {
  /** Get paginated notifications for current user */
  getAll: async (page = 0, size = 20): Promise<PageResponse<NotificationResponse>> => {
    const response = await apiClient.get('/notifications', { params: { page, size } });
    return response.data;
  },

  /** Get unread notification count */
  getUnreadCount: async (): Promise<number> => {
    const response = await apiClient.get<number>('/notifications/unread-count');
    return response.data;
  },

  /** Mark a single notification as read */
  markAsRead: async (id: number): Promise<void> => {
    await apiClient.patch(`/notifications/${id}/read`);
  },

  /** Mark all notifications as read */
  markAllAsRead: async (): Promise<void> => {
    await apiClient.patch('/notifications/read-all');
  },

  /** Clear all notifications */
  clearAll: async (): Promise<void> => {
    await apiClient.delete('/notifications');
  },

  /** Create a new notification (admin/system use) */
  create: async (data: {
    userId: number;
    title: string;
    message: string;
    type: string;
    link?: string;
  }): Promise<NotificationResponse> => {
    const response = await apiClient.post<NotificationResponse>('/notifications', data);
    return response.data;
  },
};
