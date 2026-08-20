import React from 'react';
import { Outlet } from 'react-router-dom';
import { TopNavbar } from './TopNavbar';
import { LeftSidebar } from './LeftSidebar';
import { Footer } from './Footer';
import { SessionTimeoutModal } from './SessionTimeoutModal';

export const MainLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col font-sans antialiased transition-colors duration-200">
      <TopNavbar />

      <div className="flex-1 flex w-full max-w-[1920px] mx-auto">
        <LeftSidebar />

        <main className="flex-1 p-4 sm:p-6 md:p-8 min-w-0 flex flex-col justify-between overflow-hidden">
          <div className="min-w-0">
            <Outlet />
          </div>
          <Footer />
        </main>
      </div>

      <SessionTimeoutModal />
    </div>
  );
};
