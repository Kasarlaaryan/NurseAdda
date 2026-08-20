import React from 'react';
import { FolderOpen, AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from './Button';

export interface EmptyStateProps {
  title?: string;
  description?: string;
  icon?: React.ReactNode;
  actionLabel?: string;
  onAction?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title = 'No Records Found',
  description = 'There are no items matching your criteria or filters at this time.',
  icon,
  actionLabel,
  onAction
}) => (
  <div className="flex flex-col items-center justify-center p-10 text-center rounded-2xl border border-dashed border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/40 my-4">
    <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 flex items-center justify-center mb-3">
      {icon || <FolderOpen className="w-6 h-6" />}
    </div>
    <h4 className="text-base font-semibold text-slate-800 dark:text-slate-200">{title}</h4>
    <p className="text-xs text-slate-500 dark:text-slate-400 max-w-sm mt-1">{description}</p>
    {actionLabel && onAction && (
      <Button variant="primary" size="sm" className="mt-4" onClick={onAction}>
        {actionLabel}
      </Button>
    )}
  </div>
);

export interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'System Request Failed',
  message = 'An unexpected server error occurred while retrieving data. Please retry or contact system administrator.',
  onRetry
}) => (
  <div className="flex flex-col items-center justify-center p-8 text-center rounded-2xl border border-rose-200 dark:border-rose-900/50 bg-rose-50/40 dark:bg-rose-950/20 my-4">
    <div className="w-12 h-12 rounded-full bg-rose-100 dark:bg-rose-900/60 text-rose-600 dark:text-rose-400 flex items-center justify-center mb-3">
      <AlertCircle className="w-6 h-6" />
    </div>
    <h4 className="text-base font-semibold text-rose-900 dark:text-rose-200">{title}</h4>
    <p className="text-xs text-rose-700 dark:text-rose-300 max-w-md mt-1">{message}</p>
    {onRetry && (
      <Button variant="danger" size="sm" leftIcon={<RefreshCw className="w-3.5 h-3.5" />} className="mt-4" onClick={onRetry}>
        Retry Action
      </Button>
    )}
  </div>
);
