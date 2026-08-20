import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { UserRole } from '../types';

interface RoleRouteProps {
  allowedRoles: UserRole[];
}

export const RoleRoute: React.FC<RoleRouteProps> = ({ allowedRoles }) => {
  const { activeRole } = useAuthStore();

  if (!allowedRoles.includes(activeRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};
