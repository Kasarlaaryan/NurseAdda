import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

export const Breadcrumb: React.FC = () => {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  return (
    <nav className="flex items-center space-x-2 text-xs text-slate-500 dark:text-slate-400 py-2">
      <Link
        to="/dashboard"
        className="flex items-center gap-1 hover:text-amber-600 dark:hover:text-amber-400 transition-colors"
      >
        <Home className="w-3.5 h-3.5" />
        <span>NurseAdda</span>
      </Link>
      {pathnames.map((name, index) => {
        const routeTo = `/${pathnames.slice(0, index + 1).join('/')}`;
        const isLast = index === pathnames.length - 1;
        const formattedName = name
          .replace(/-/g, ' ')
          .replace(/\b\w/g, (char) => char.toUpperCase());

        return (
          <React.Fragment key={name}>
            <ChevronRight className="w-3 h-3 text-slate-400 shrink-0" />
            {isLast ? (
              <span className="font-semibold text-slate-800 dark:text-slate-200">{formattedName}</span>
            ) : (
              <Link
                to={routeTo}
                className="hover:text-amber-600 dark:hover:text-amber-400 transition-colors"
              >
                {formattedName}
              </Link>
            )}
          </React.Fragment>
        );
      })}
    </nav>
  );
};
