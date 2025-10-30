import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { env } from '../config/env';

const SignupPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== passwordConfirm) {
      setError('Password và PasswordConfirm phải giống nhau');
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await fetch(`${env.apiBaseUrl}/api/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), password, passwordConfirm }),
      });

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `Signup failed: ${res.status}`);
      }

      // On success, redirect to login
      navigate('/login', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Không thể đăng ký');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: 32 }}>
      <form onSubmit={handleSubmit} style={{ width: 480, background: '#fff', padding: 24, borderRadius: 8 }}>
        <h2>Đăng ký</h2>
        <div style={{ marginTop: 12 }}>
          <label>Email</label>
          <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required style={{ width: '100%', padding: 8 }} />
        </div>
        <div style={{ marginTop: 12 }}>
          <label>Password</label>
          <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required style={{ width: '100%', padding: 8 }} />
        </div>
        <div style={{ marginTop: 12 }}>
          <label>Confirm Password</label>
          <input value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)} type="password" required style={{ width: '100%', padding: 8 }} />
        </div>
        {error ? <div style={{ color: '#b02a37', marginTop: 12 }}>{error}</div> : null}
        <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
          <button type="submit" disabled={isSubmitting} style={{ padding: '8px 16px' }}>{isSubmitting ? 'Đang đăng ký...' : 'Đăng ký'}</button>
          <button type="button" onClick={() => navigate('/login')} style={{ padding: '8px 16px' }}>Quay lại đăng nhập</button>
        </div>
      </form>
    </div>
  );
};

export default SignupPage;
