import React, { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { tokenStorage } from '../services/apiClient';
import { Loader } from '../components/common/Loader';

/**
 * Routes that staff with an incomplete profile are allowed to visit.
 * Everything else redirects to /complete-profile.
 */
const STAFF_PROFILE_ROUTES = ['/complete-profile', '/profile', '/login'];

export const ProtectedRoute: React.FC = () => {
  const { isAuthenticated, user, fetchCurrentUser } = useAuthStore();
  const location = useLocation();
  const [checking, setChecking] = useState(!isAuthenticated);

  useEffect(() => {
    // If not authenticated in Zustand but tokens exist, try to restore session
    if (!isAuthenticated && tokenStorage.getAccessToken()) {
      fetchCurrentUser().finally(() => setChecking(false));
    } else {
      setChecking(false);
    }
  }, [isAuthenticated, fetchCurrentUser]);

  if (checking) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Loader />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Enforce profile completion for staff users
  if (
    user?.role === 'ROLE_STAFF' &&
    user?.isProfileComplete === false &&
    !STAFF_PROFILE_ROUTES.includes(location.pathname)
  ) {
    return <Navigate to="/complete-profile" replace />;
  }

  return <Outlet />;
};
