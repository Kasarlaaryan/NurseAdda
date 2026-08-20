import React from 'react';
import { Loader2 } from 'lucide-react';

export interface SkeletonProps {
  className?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({ className = '' }) => (
  <div className={`animate-pulse rounded-lg bg-slate-200 dark:bg-slate-800 ${className}`} />
);

export const Loader: React.FC<{ message?: string; fullPage?: boolean }> = ({ message = 'Loading system data...', fullPage = false }) => {
  if (fullPage) {
    return (
      <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-white/80 dark:bg-slate-900/80 backdrop-blur-xs">
        <div className="flex flex-col items-center gap-3 p-6 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xl">
          <Loader2 className="w-8 h-8 animate-spin text-teal-600 dark:text-teal-400" />
          <p className="text-sm font-medium text-slate-700 dark:text-slate-300">{message}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center p-12 gap-3 text-center">
      <Loader2 className="w-8 h-8 animate-spin text-teal-600 dark:text-teal-400" />
      <p className="text-sm font-medium text-slate-600 dark:text-slate-400">{message}</p>
    </div>
  );
};
