import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../components/common/Button';
import { FileQuestion, ArrowLeft } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center p-6 space-y-4">
      <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-400 flex items-center justify-center">
        <FileQuestion className="w-8 h-8" />
      </div>
      <h1 className="text-3xl font-black text-slate-900 dark:text-slate-100">404 - Page Not Found</h1>
      <p className="text-xs sm:text-sm text-slate-500 max-w-md">
        The system page or resource you requested could not be located in NurseAdda WFM.
      </p>
      <Link to="/dashboard">
        <Button variant="primary" size="md" leftIcon={<ArrowLeft className="w-4 h-4" />}>
          Return to Dashboard
        </Button>
      </Link>
    </div>
  );
};
