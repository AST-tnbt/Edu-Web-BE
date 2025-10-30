import { useMemo, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';

import type { LoginCredentials } from '../types/auth';
import './LoginForm.css';

interface LoginFormProps {
  onSubmit: (credentials: LoginCredentials) => Promise<void> | void;
  isSubmitting?: boolean;
  error?: string | null;
  onClearError?: () => void;
}

const LoginForm = ({ onSubmit, isSubmitting = false, error, onClearError }: LoginFormProps) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const isDisabled = useMemo(() => {
    return isSubmitting || !email.trim() || !password;
  }, [email, isSubmitting, password]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isDisabled) {
      return;
    }
    await onSubmit({ email: email.trim(), password });
  };

  const handleEmailChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (onClearError) {
      onClearError();
    }
    setEmail(event.target.value);
  };

  const handlePasswordChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (onClearError) {
      onClearError();
    }
    setPassword(event.target.value);
  };

  return (
    <div className="login-form-container">
      <form className="login-form" onSubmit={handleSubmit} noValidate>
        <h2>Sign in to account</h2>
        <p className="subtitle">Enter your email &amp; password to login</p>

        <div className="input-group">
          <label htmlFor="email">Email Address</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={handleEmailChange}
            placeholder="Enter Your Email"
            autoComplete="email"
            required
          />
        </div>

        <div className="input-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={handlePasswordChange}
            placeholder="Enter Password"
            autoComplete="current-password"
            required
          />
        </div>

        {error ? (
          <div className="form-error" role="alert">
            {error}
          </div>
        ) : null}

        <a href="#" className="forgot-password">Forgot Password?</a>

        <button className="btn-signin" type="submit" disabled={isDisabled}>
          {isSubmitting ? 'Signing in...' : 'Sign in'}
        </button>

        <p className="signup-link">
          Don't have account? <a href="/signup">Create Account</a>
        </p>
      </form>
    </div>
  );
};

export default LoginForm;
