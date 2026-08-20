import React from 'react';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { useNotificationStore } from '../store/useNotificationStore';
import { useToast } from '../components/common/Toast';
import { Bell, CheckCheck, Trash2, Calendar, ShieldAlert } from 'lucide-react';

export const NotificationsPage: React.FC = () => {
  const { notifications, markAllAsRead, clearAll, markAsRead } = useNotificationStore();
  const { showToast } = useToast();

  const handleMarkAllRead = () => {
    markAllAsRead();
    showToast('success', 'Notifications Updated', 'All notifications have been marked as read.');
  };

  const handleClearAll = () => {
    clearAll();
    showToast('info', 'Notifications Cleared', 'All notifications have been removed from your queue.');
  };

  const handleMarkAsRead = (id: string) => {
    markAsRead(id);
    showToast('success', 'Notification Read', 'Notification marked as read.');
  };

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

      <Card className="divide-y divide-slate-100 dark:divide-slate-800">
        {notifications.length === 0 ? (
          <div className="p-10 text-center text-slate-500 text-xs">
            No active notifications in your queue.
          </div>
        ) : (
          notifications.map((notif) => (
            <div
              key={notif.id}
              className={`p-4 flex items-start justify-between gap-4 transition-colors ${
                !notif.read ? 'bg-teal-50/40 dark:bg-teal-950/20' : ''
              }`}
            >
              <div className="flex items-start gap-3">
                <div className="w-9 h-9 rounded-xl bg-teal-100 dark:bg-teal-900/60 text-teal-700 dark:text-teal-300 flex items-center justify-center shrink-0 mt-0.5">
                  <Bell className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h4 className="font-bold text-sm text-slate-900 dark:text-slate-100">{notif.title}</h4>
                    {!notif.read && <Badge variant="primary" size="sm">New</Badge>}
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-400 mt-1">{notif.message}</p>
                  <span className="text-[10px] text-slate-400 mt-1 block">{notif.timestamp}</span>
                </div>
              </div>

              {!notif.read && (
                <Button variant="ghost" size="sm" onClick={() => handleMarkAsRead(notif.id)}>
                  Mark Read
                </Button>
              )}
            </div>
          ))
        )}
      </Card>
    </div>
  );
};
