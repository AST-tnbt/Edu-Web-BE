import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import type { Location } from 'react-router-dom';

import LoginForm from '../components/LoginForm';
import { useAuth } from '../hooks/useAuth';
import type { LoginCredentials } from '../types/auth';

const LoginPage = () => {
  const { login, isAuthenticated, isAuthenticating } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);

  const fromPath = useMemo(() => {
    const state = location.state as { from?: Location } | undefined;
    return state?.from?.pathname ?? '/';
  }, [location]);

  useEffect(() => {
    if (isAuthenticated) {
      navigate(fromPath, { replace: true });
    }
  }, [fromPath, isAuthenticated, navigate]);

  const handleSubmit = async ({ email, password }: LoginCredentials) => {
    setError(null);
    try {
      await login({ email, password });
      navigate(fromPath, { replace: true });
    } catch (authError) {
      const message = authError instanceof Error ? authError.message : 'Không thể đăng nhập. Vui lòng thử lại.';
      setError(message);
    }
  };

  return (
    <LoginForm
      onSubmit={handleSubmit}
      isSubmitting={isAuthenticating}
      error={error}
      onClearError={() => setError(null)}
    />
  );
};

export default LoginPage;
