import { create } from 'zustand';
import { NotificationItem } from '../types';
import { MOCK_NOTIFICATIONS } from '../services/mockData';

interface NotificationState {
  notifications: NotificationItem[];
  unreadCount: number;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  addNotification: (notification: Omit<NotificationItem, 'id' | 'timestamp' | 'read'>) => void;
  clearAll: () => void;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: MOCK_NOTIFICATIONS,
  unreadCount: MOCK_NOTIFICATIONS.filter(n => !n.read).length,

  markAsRead: (id: string) => {
    const updated = get().notifications.map(n => n.id === id ? { ...n, read: true } : n);
    set({
      notifications: updated,
      unreadCount: updated.filter(n => !n.read).length
    });
  },

  markAllAsRead: () => {
    const updated = get().notifications.map(n => ({ ...n, read: true }));
    set({
      notifications: updated,
      unreadCount: 0
    });
  },

  addNotification: (item) => {
    const newNotif: NotificationItem = {
      ...item,
      id: `notif-${Date.now()}`,
      timestamp: 'Just now',
      read: false
    };
    const updated = [newNotif, ...get().notifications];
    set({
      notifications: updated,
      unreadCount: updated.filter(n => !n.read).length
    });
  },

  clearAll: () => {
    set({ notifications: [], unreadCount: 0 });
  }
}));
