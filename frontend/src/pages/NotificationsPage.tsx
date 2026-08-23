import React, { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { useNotificationStore } from '../store/useNotificationStore';
import { useToast } from '../components/common/Toast';
import { notificationService } from '../services/notificationService';
import { Bell, CheckCheck, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';

export const NotificationsPage: React.FC = () => {
  const { notifications, markAllAsRead, clearAll, markAsRead, fetchNotifications, fetchUnreadCount } =
    useNotificationStore();
  const { showToast } = useToast();

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationService.getAll(0, 50),
  });

  useEffect(() => {
    if (data) {
      fetchNotifications();
      fetchUnreadCount();
    }
  }, [data, fetchNotifications, fetchUnreadCount]);

  const handleMarkAllRead = async () => {
    await markAllAsRead();
    showToast('success', 'Notifications Updated', 'All notifications have been marked as read.');
  };

  const handleClearAll = async () => {
    await clearAll();
    showToast('info', 'Notifications Cleared', 'All notifications have been removed from your queue.');
  };

  const handleMarkAsRead = async (id: string) => {
    await markAsRead(id);
    showToast('success', 'Notification Read', 'Notification marked as read.');
  };

  const notifs = data?.content || notifications;

  return (
    <div className="space-y-6">
      <PageHeader
        title="System Notifications & Alerts"
        description="Shift assignment alerts, credential verification reminders, and payment updates."
        actions={
          <div className="flex gap-2">
            <Button variant="outline" size="sm" leftIcon={<CheckCheck className="w-4 h-4" />} onClick={handleMarkAllRead}>
              Mark All Read
            </Button>
            <Button variant="ghost" size="sm" leftIcon={<Trash2 className="w-4 h-4" />} onClick={handleClearAll}>
              Clear All
            </Button>
          </div>
        }
      />

      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Loader />
        </div>
      ) : notifs.length === 0 ? (
        <EmptyState
          title="No notifications"
          description="You're all caught up! Notifications will appear here when there are updates."
        />
      ) : (
        <Card className="divide-y divide-slate-100 dark:divide-slate-800">
          {notifs.map((notif) => (
            <div
              key={notif.id}
              className={`p-4 flex items-start justify-between gap-4 transition-colors ${
                !notif.read ? 'bg-amber-50/40 dark:bg-amber-950/20' : ''
              }`}
            >
              <div className="flex items-start gap-3">
                <div className="w-9 h-9 rounded-xl bg-amber-100 dark:bg-amber-900/60 text-amber-700 dark:text-amber-300 flex items-center justify-center shrink-0 mt-0.5">
                  <Bell className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h4 className="font-bold text-sm text-slate-900 dark:text-slate-100">{notif.title}</h4>
                    {!notif.read && <Badge variant="primary" size="sm">New</Badge>}
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-400 mt-1">{notif.message}</p>
                  <div className="flex items-center gap-3 mt-1">
                    <span className="text-[10px] text-slate-400">{notif.timestamp}</span>
                    {notif.link && (
                      <Link
                        to={notif.link}
                        className="text-[10px] text-amber-500 font-semibold hover:underline"
                      >
                        View →
                      </Link>
                    )}
                  </div>
                </div>
              </div>

              {!notif.read && (
                <Button variant="ghost" size="sm" onClick={() => handleMarkAsRead(notif.id)}>
                  Mark Read
                </Button>
              )}
            </div>
          ))}
        </Card>
      )}
    </div>
  );
};
