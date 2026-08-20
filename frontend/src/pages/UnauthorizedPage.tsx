import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../components/common/Button';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export const UnauthorizedPage: React.FC = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6 space-y-4">
      <div className="w-16 h-16 rounded-full bg-rose-100 dark:bg-rose-950/60 text-rose-600 dark:text-rose-400 flex items-center justify-center">
        <ShieldAlert className="w-8 h-8" />
      </div>
      <h1 className="text-3xl font-black text-slate-900 dark:text-slate-100">403 - Access Restricted</h1>
      <p className="text-xs sm:text-sm text-slate-500 max-w-md">
        Your current role persona does not possess the required privilege level to access this healthcare module. Please switch role personas in the top bar to evaluate.
      </p>
      <Link to="/dashboard">
        <Button variant="primary" size="md" leftIcon={<ArrowLeft className="w-4 h-4" />}>
          Return to Dashboard
        </Button>
      </Link>
    </div>
  );
};
