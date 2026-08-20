import React, { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { tokenStorage } from '../services/apiClient';
import { Loader } from '../components/common/Loader';

export const ProtectedRoute: React.FC = () => {
  const { isAuthenticated, fetchCurrentUser } = useAuthStore();
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

  return <Outlet />;
};
