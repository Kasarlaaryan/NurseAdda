import React from 'react';
import { NavLink } from 'react-router-dom';
import { NAVIGATION_ITEMS, USER_ROLES_CONFIG } from '../../constants';
import { useAuthStore } from '../../store/useAuthStore';
import { useAppStore } from '../../store/useAppStore';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export const LeftSidebar: React.FC = () => {
  const { activeRole } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar, mobileSidebarOpen, setMobileSidebarOpen } = useAppStore();

  // Filter items based on user's active role
  const filteredNavItems = NAVIGATION_ITEMS.filter((item) =>
    item.roles.includes(activeRole)
  );

  const roleConfig = USER_ROLES_CONFIG[activeRole] || USER_ROLES_CONFIG['ROLE_SUPER_ADMIN'];

  return (
    <>
      {/* Mobile Backdrop */}
      {mobileSidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs md:hidden"
          onClick={() => setMobileSidebarOpen(false)}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed md:sticky top-16 left-0 z-50 h-[calc(100vh-4rem)] bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 border-r border-slate-200 dark:border-slate-800 flex flex-col justify-between transition-all duration-300 ${
          sidebarCollapsed ? 'w-20' : 'w-64'
        } ${
          mobileSidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        {/* Collapse Toggle Button */}
        <button
          onClick={toggleSidebar}
          className="hidden md:flex absolute -right-3 top-6 w-6 h-6 rounded-full bg-amber-600 text-white border-2 border-white dark:border-slate-900 items-center justify-center shadow-sm hover:bg-amber-500 transition-colors z-50"
          title={sidebarCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
        >
          {sidebarCollapsed ? <ChevronRight className="w-3.5 h-3.5" /> : <ChevronLeft className="w-3.5 h-3.5" />}
        </button>

        {/* Navigation Items */}
        <div className="flex-1 py-4 px-3 overflow-y-auto space-y-1">
          {/* Active Role Identifier Header */}
          {!sidebarCollapsed && (
            <div className="px-3 py-2 mb-2 rounded-xl bg-amber-50/80 dark:bg-amber-950/40 border border-amber-100 dark:border-amber-800/50">
              <span className="text-[10px] uppercase font-bold tracking-wider text-amber-700 dark:text-amber-300 block">
                Active Portal
              </span>
              <span className="text-xs font-semibold text-slate-900 dark:text-slate-100 block truncate">
                {roleConfig.label}
              </span>
            </div>
          )}

          {filteredNavItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.href}
                to={item.href}
                onClick={() => setMobileSidebarOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all duration-150 ${
                    isActive
                      ? 'bg-amber-50 text-amber-600 dark:bg-amber-950/80 dark:text-amber-300 font-bold shadow-2xs'
                      : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800/60'
                  } ${sidebarCollapsed ? 'justify-center px-0' : ''}`
                }
                title={sidebarCollapsed ? item.title : undefined}
              >
                <Icon className="w-4 h-4 shrink-0" />
                {!sidebarCollapsed && <span className="truncate flex-1">{item.title}</span>}
                {!sidebarCollapsed && item.badge && (
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 dark:bg-amber-900/60 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800/50">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </div>


      </aside>
    </>
  );
};
