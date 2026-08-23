import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { notificationService, NotificationResponse } from '../services/notificationService';

/** Map backend notification to frontend shape for backward compat */
interface NotificationItem {
  id: string;
  title: string;
  message: string;
  type: 'assignment' | 'approval' | 'attendance' | 'system' | 'payment';
  timestamp: string;
  read: boolean;
  link?: string;
}

function mapNotification(n: NotificationResponse): NotificationItem {
  const typeMap: Record<string, NotificationItem['type']> = {
    assignment: 'assignment',
    approval: 'approval',
    attendance: 'attendance',
    payment: 'payment',
  };
  return {
    id: String(n.id),
    title: n.title,
    message: n.message,
    type: typeMap[n.type] || 'system',
    timestamp: formatTimestamp(n.createdAt),
    read: n.read,
    link: n.link,
  };
}

function formatTimestamp(isoDate: string): string {
  const date = new Date(isoDate);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins} minutes ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours} hours ago`;
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays === 1) return 'Yesterday';
  return `${diffDays} days ago`;
}

interface NotificationState {
  notifications: NotificationItem[];
  unreadCount: number;
  isLoading: boolean;
  fetchNotifications: () => Promise<void>;
  fetchUnreadCount: () => Promise<void>;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  clearAll: () => Promise<void>;
}

export const useNotificationStore = create<NotificationState>()(
  persist(
    (set, get) => ({
      notifications: [],
      unreadCount: 0,
      isLoading: false,

      fetchNotifications: async () => {
        set({ isLoading: true });
        try {
          const data = await notificationService.getAll(0, 50);
          const notifications = (data?.content || []).map(mapNotification);
          set({ notifications, isLoading: false });
        } catch {
          // Backend may not have notifications table yet — fail silently
          set({ notifications: [], isLoading: false });
        }
      },

      fetchUnreadCount: async () => {
        try {
          const count = await notificationService.getUnreadCount();
          set({ unreadCount: count || 0 });
        } catch {
          // Backend may not have notifications table yet — fail silently
          set({ unreadCount: 0 });
        }
      },

      markAsRead: async (id: string) => {
        try {
          await notificationService.markAsRead(Number(id));
          const updated = get().notifications.map((n) =>
            n.id === id ? { ...n, read: true } : n
          );
          set({
            notifications: updated,
            unreadCount: updated.filter((n) => !n.read).length,
          });
        } catch {
          // silently fail
        }
      },

      markAllAsRead: async () => {
        try {
          await notificationService.markAllAsRead();
          const updated = get().notifications.map((n) => ({ ...n, read: true }));
          set({ notifications: updated, unreadCount: 0 });
        } catch {
          // silently fail
        }
      },

      clearAll: async () => {
        try {
          await notificationService.clearAll();
          set({ notifications: [], unreadCount: 0 });
        } catch {
          // silently fail
        }
      },
    }),
    {
      name: 'nurseadda-notifications',
      partialize: (state) => ({
        notifications: state.notifications,
        unreadCount: state.unreadCount,
      }),
    }
  )
);
