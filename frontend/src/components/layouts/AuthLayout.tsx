import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { ShieldCheck, HeartPulse, Building2, UserCheck } from 'lucide-react';

export const AuthLayout: React.FC = () => {
  return (
    <div className="min-h-screen w-full bg-slate-900 text-slate-100 flex flex-col justify-between relative overflow-hidden">
      {/* Abstract Background Glows */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Header Bar */}
      <header className="p-6 max-w-7xl mx-auto w-full flex items-center justify-between z-10">
        <Link to="/" className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-sky-500 text-slate-950 flex items-center justify-center font-extrabold text-xl shadow-lg shadow-sky-500/20">
            N+
          </div>
          <div>
            <span className="font-extrabold text-xl tracking-tight text-white block leading-none">
              NurseAdda
            </span>
            <span className="text-[11px] uppercase font-bold tracking-widest text-sky-400 mt-1 block">
              Enterprise Workforce OS
            </span>
          </div>
        </Link>

        <div className="hidden sm:flex items-center gap-2 text-xs font-semibold text-slate-400 bg-slate-800/80 px-3 py-1.5 rounded-full border border-slate-700/60">
          <ShieldCheck className="w-4 h-4 text-sky-400" />
          <span>SOC-2 & HIPAA Certified Portal</span>
        </div>
      </header>

      {/* Main Form Body */}
      <main className="flex-1 flex items-center justify-center px-4 sm:px-6 py-12 z-10">
        <div className="w-full max-w-4xl">
          <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-10 shadow-2xl backdrop-blur-xl">
            <Outlet />
          </div>
        </div>
      </main>

      {/* Footer Bar */}
      <footer className="p-6 text-center text-xs text-slate-500 z-10">
        <p>© {new Date().getFullYear()} NurseAdda Healthcare Technologies. All Rights Reserved.</p>
      </footer>
    </div>
  );
};
