import React from 'react';

export interface AvatarProps {
  src?: string;
  name: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  status?: 'online' | 'offline' | 'away' | 'busy';
  className?: string;
}

export const Avatar: React.FC<AvatarProps> = ({
  src,
  name,
  size = 'md',
  status,
  className = ''
}) => {
  const initials = name
    .split(' ')
    .map((n) => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  const sizeClasses = {
    sm: 'w-7 h-7 text-xs',
    md: 'w-9 h-9 text-xs',
    lg: 'w-11 h-11 text-sm font-semibold',
    xl: 'w-14 h-14 text-base font-bold'
  };

  const statusColors = {
    online: 'bg-emerald-500 ring-2 ring-white dark:ring-slate-900',
    offline: 'bg-slate-400 ring-2 ring-white dark:ring-slate-900',
    away: 'bg-amber-500 ring-2 ring-white dark:ring-slate-900',
    busy: 'bg-rose-500 ring-2 ring-white dark:ring-slate-900'
  };

  return (
    <div className={`relative inline-block shrink-0 ${className}`}>
      {src ? (
        <img
          src={src}
          alt={name}
          className={`${sizeClasses[size]} rounded-full object-cover border border-slate-200 dark:border-slate-700`}
        />
      ) : (
        <div
          className={`${sizeClasses[size]} rounded-full bg-teal-100 dark:bg-teal-900/60 text-teal-800 dark:text-teal-200 font-semibold flex items-center justify-center border border-teal-200 dark:border-teal-800`}
        >
          {initials}
        </div>
      )}

      {status && (
        <span
          className={`absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full ${statusColors[status]}`}
        />
      )}
    </div>
  );
};
