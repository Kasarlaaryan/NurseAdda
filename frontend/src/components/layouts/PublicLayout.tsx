import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import { Button } from '../common/Button';
import { Activity } from 'lucide-react';
import { useAuthStore } from '../../store/useAuthStore';

export const PublicLayout: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <header className="fixed top-0 w-full z-50 bg-white/80 dark:bg-slate-950/80 backdrop-blur-xl border-b border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-amber-600 rounded-lg flex items-center justify-center">
              <Activity className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-black text-slate-900 dark:text-white tracking-tighter">
              NurseAdda<span className="text-amber-600">WFM</span>
            </span>
          </Link>
          
          <nav className="hidden md:flex items-center gap-8 text-sm font-bold text-slate-600 dark:text-slate-400">
            <Link to="/about" className="hover:text-amber-600 transition-colors">About</Link>
            <Link to="/services" className="hover:text-amber-600 transition-colors">Services</Link>
            <Link to="/contact" className="hover:text-amber-600 transition-colors">Contact</Link>
          </nav>

          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <Link to="/dashboard">
                <Button variant="primary" size="sm" className="shadow-lg shadow-amber-500/20">
                  Dashboard
                </Button>
              </Link>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">Sign In</Button>
                </Link>
                <Link to="/register">
                  <Button variant="primary" size="sm" className="shadow-lg shadow-amber-500/20">
                    Get Started
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
};
