import { Navigate, useLocation } from 'react-router-dom';
import type { ReactElement } from 'react';

import { useAuth } from '../hooks/useAuth';

interface ProtectedRouteProps {
  children: ReactElement;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const { isAuthenticated, isAuthenticating } = useAuth();
  const location = useLocation();

  if (isAuthenticating) {
    return (
      <div className="route-loading" role="status" aria-live="polite">
        Đang xác thực...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return children;
};

export default ProtectedRoute;
