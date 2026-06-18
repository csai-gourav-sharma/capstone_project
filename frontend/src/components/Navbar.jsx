import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) return null;

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">📦 SMS</Link>
      </div>
      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/catalog">Catalog</Link>
        {user?.role === 'STUDENT' && <Link to="/my-requests">My Requests</Link>}
        {user?.role === 'ADMIN' && (
          <>
            <Link to="/admin/inventory">Inventory</Link>
            <Link to="/admin/requests">Requests</Link>
          </>
        )}
      </div>
      <div className="navbar-user">
        <span className="user-badge">{user?.role}</span>
        <span className="user-email">{user?.email}</span>
        <button onClick={handleLogout} className="btn btn-logout">Logout</button>
      </div>
    </nav>
  );
}
