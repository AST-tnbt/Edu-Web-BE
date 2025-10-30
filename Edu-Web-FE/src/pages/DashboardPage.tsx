import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleSignOut = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const rawRole = user?.role?.replace(/^ROLE_/i, '') ?? 'UNKNOWN';
  const roleLabel = rawRole
    .replace(/_/g, ' ')
    .toLowerCase()
    .split(' ')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Xin chào, {user?.email ?? 'người dùng'}</h1>
          <p className="dashboard-subtitle">Vai trò hiện tại: {roleLabel}</p>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <button type="button" className="btn-signout" onClick={() => navigate('/profile')}>
            Hồ sơ
          </button>
          <button type="button" className="btn-signout" onClick={handleSignOut}>
            Đăng xuất
          </button>
        </div>
      </header>
      <section className="dashboard-content">
        <p>Chọn một chức năng trong menu để bắt đầu làm việc với hệ thống.</p>
      </section>
    </div>
  );
};

export default DashboardPage;
