import React, { useState, useEffect, useRef } from 'react';
import { useAuthStore } from '../../store/useAuthStore';
import { useThemeStore } from '../../store/useThemeStore';
import { useNotificationStore } from '../../store/useNotificationStore';
import { useAppStore } from '../../store/useAppStore';
import { USER_ROLES_CONFIG } from '../../constants';
import {
  Menu,
  Sun,
  Moon,
  Bell,
  Search,
  LogOut,
  Shield,
  Activity,
  User as UserIcon
} from 'lucide-react';
import { Avatar } from '../common/Avatar';
import { Badge } from '../common/Badge';
import { Link, useNavigate } from 'react-router-dom';
import { playNotificationSound } from '../../utils/notificationSound';

export const TopNavbar: React.FC = () => {
  const navigate = useNavigate();
  const { user, activeRole, logout } = useAuthStore();
  const { theme, toggleTheme } = useThemeStore();
  const { unreadCount, notifications, markAllAsRead, fetchNotifications, fetchUnreadCount } = useNotificationStore();
  const { toggleSidebar, toggleMobileSidebar, globalSearchQuery, setGlobalSearchQuery } = useAppStore();

  const [showNotifMenu, setShowNotifMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const prevUnreadCount = useRef(unreadCount);

  // Play sound when unread count increases
  useEffect(() => {
    if (unreadCount > prevUnreadCount.current && prevUnreadCount.current >= 0) {
      playNotificationSound();
    }
    prevUnreadCount.current = unreadCount;
  }, [unreadCount]);

  // Fetch notifications on mount and poll unread count every 30s
  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
    const interval = setInterval(() => {
      fetchUnreadCount();
    }, 30000);
    return () => clearInterval(interval);
  }, [fetchNotifications, fetchUnreadCount]);

  // Also refresh when notification menu is opened
  const handleNotifMenuToggle = () => {
    if (!showNotifMenu) {
      fetchNotifications();
      fetchUnreadCount();
    }
    setShowNotifMenu(!showNotifMenu);
  };

  const roleConfig = USER_ROLES_CONFIG[activeRole] || USER_ROLES_CONFIG['ROLE_SUPER_ADMIN'];

  return (
    <header className="sticky top-0 z-30 w-full h-16 bg-white/95 dark:bg-slate-900/95 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800 transition-colors">
      <div className="h-full px-4 sm:px-6 flex items-center justify-between gap-4">
        {/* Left Section: Brand & Sidebar Toggle */}
        <div className="flex items-center gap-3">
          <button
            onClick={() => {
              toggleSidebar();
              toggleMobileSidebar();
            }}
            className="p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors focus:outline-none"
            title="Toggle Sidebar"
          >
            <Menu className="w-5 h-5" />
          </button>

          <Link to="/dashboard" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-600 dark:bg-amber-500 text-white flex items-center justify-center font-bold text-lg shadow-xs">
              N+
            </div>
            <div className="hidden sm:flex flex-col">
              <span className="font-extrabold text-slate-900 dark:text-slate-100 tracking-tight text-base leading-none">
                NurseAdda
              </span>
              <span className="text-[10px] uppercase font-semibold tracking-wider text-amber-600 dark:text-amber-400 mt-0.5">
                Workforce OS
              </span>
            </div>
          </Link>
        </div>

        {/* Center Section: Global Search Bar */}
        <div className="hidden md:flex flex-1 max-w-md mx-4">
          <div className="relative w-full">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={globalSearchQuery}
              onChange={(e) => setGlobalSearchQuery(e.target.value)}
              placeholder="Search staff, client hospitals, shift requests, invoice #..."
              className="w-full pl-10 pr-4 py-2 text-xs rounded-xl bg-slate-100/80 dark:bg-slate-800/80 border border-transparent focus:border-amber-500 focus:bg-white dark:focus:bg-slate-900 text-slate-900 dark:text-slate-100 placeholder:text-slate-400 transition-all focus:outline-none"
            />
          </div>
        </div>

        {/* Right Section: Role Switcher, System Ticker, Theme Toggle, Notifications, Profile */}
        <div className="flex items-center gap-2 sm:gap-3">
          {/* Active System Status Ticker */}
          <div className="hidden xl:flex items-center gap-2 px-2.5 py-1 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200/60 dark:border-emerald-800/40 text-[11px] font-medium">
            <Activity className="w-3.5 h-3.5 animate-pulse" />
            <span>99.8% Shifts Matched</span>
          </div>

          {/* Active Role Badge (read-only) */}
          <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl border border-slate-200 dark:border-slate-800">
            <Shield className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400" />
            <span className="text-xs font-semibold text-slate-700 dark:text-slate-300">
              {roleConfig.label}
            </span>
          </div>

          {/* Theme Toggle Button */}
          <button
            onClick={toggleTheme}
            className="p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            title={`Switch to ${theme === 'light' ? 'Dark' : 'Light'} Mode`}
          >
            {theme === 'light' ? <Moon className="w-4 h-4" /> : <Sun className="w-4 h-4 text-amber-400" />}
          </button>

          {/* Notifications Center Menu */}
          <div className="relative">
            <button
              onClick={handleNotifMenuToggle}
              className="relative p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
              title="Notifications"
            >
              <Bell className="w-4 h-4" />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center px-1 shadow-lg shadow-rose-500/30">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              )}
            </button>

            {showNotifMenu && (
              <div
                className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xl p-3 z-50 animate-in fade-in zoom-in-95 duration-150"
              >
                <div className="flex items-center justify-between px-2 pb-2 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-xs font-bold text-slate-900 dark:text-slate-100">System Notifications</span>
                  {unreadCount > 0 && (
                    <button
                      onClick={markAllAsRead}
                      className="text-[11px] text-amber-600 dark:text-amber-400 font-semibold hover:underline"
                    >
                      Mark all read
                    </button>
                  )}
                </div>
                <div className="py-2 max-h-72 overflow-y-auto space-y-2">
                  {notifications.slice(0, 5).map((n) => (
                    <div
                      key={n.id}
                      className={`p-2.5 rounded-xl border text-xs transition-colors ${
                        !n.read
                          ? 'bg-amber-50/50 dark:bg-amber-950/30 border-amber-200 dark:border-amber-900/40'
                          : 'bg-slate-50/40 dark:bg-slate-800/40 border-slate-100 dark:border-slate-800'
                      }`}
                    >
                      <p className="font-semibold text-slate-900 dark:text-slate-100">{n.title}</p>
                      <p className="text-slate-600 dark:text-slate-400 mt-0.5">{n.message}</p>
                      <span className="text-[10px] text-slate-400 block mt-1">{n.timestamp}</span>
                    </div>
                  ))}
                </div>
                <Link
                  to="/notifications"
                  onClick={() => setShowNotifMenu(false)}
                  className="block text-center text-xs font-semibold text-amber-600 dark:text-amber-400 py-1.5 hover:underline border-t border-slate-100 dark:border-slate-800 mt-1"
                >
                  View All Notifications
                </Link>
              </div>
            )}
          </div>

          {/* Profile Dropdown */}
          <div className="relative">
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className="flex items-center gap-2 pl-2 pr-1.5 py-1 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <Avatar
                name={user?.name || 'Admin User'}
                src={user?.avatarUrl}
                size="sm"
                status="online"
              />
              <div className="hidden lg:flex flex-col text-left">
                <span className="text-xs font-semibold text-slate-900 dark:text-slate-100 leading-tight">
                  {user?.name}
                </span>
                <span className="text-[10px] text-slate-500 dark:text-slate-400">
                  {roleConfig.label}
                </span>
              </div>
            </button>

            {showUserMenu && (
              <div
                className="absolute right-0 mt-2 w-56 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xl p-2 z-50 animate-in fade-in zoom-in-95 duration-150"
                onClick={() => setShowUserMenu(false)}
              >
                <div className="px-3 py-2 border-b border-slate-100 dark:border-slate-800">
                  <p className="text-xs font-bold text-slate-900 dark:text-slate-100">{user?.name}</p>
                  <p className="text-[11px] text-slate-500 truncate">{user?.email}</p>
                  <div className="mt-1.5">
                    <Badge size="sm" variant="primary">{roleConfig.label}</Badge>
                  </div>
                </div>

                <div className="py-1">
                  <Link
                    to="/profile"
                    className="flex items-center gap-2 px-3 py-2 text-xs font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl"
                  >
                    <UserIcon className="w-3.5 h-3.5 text-slate-400" />
                    My Account Profile
                  </Link>
                  <button
                    onClick={() => {
                      logout();
                      navigate('/login');
                    }}
                    className="w-full flex items-center gap-2 px-3 py-2 text-xs font-semibold text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 rounded-xl mt-1"
                  >
                    <LogOut className="w-3.5 h-3.5" />
                    Sign Out
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
