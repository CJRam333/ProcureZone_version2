import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { LoadingSpinner } from '../components/common';

interface ProtectedRouteProps {
  children: React.ReactNode;
  roles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, roles }) => {
  const { isAuthenticated, isLoading, hasAnyRole } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingSpinner fullPage text="Checking authentication..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles && roles.length > 0 && !hasAnyRole(roles)) {
    return (
      <div className="text-center py-5">
        <h2>Access Denied</h2>
        <p className="text-muted">
          You don't have permission to access this page.
        </p>
      </div>
    );
  }

  return <>{children}</>;
};

export default ProtectedRoute;
