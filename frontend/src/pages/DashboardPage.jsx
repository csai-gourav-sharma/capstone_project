import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';

export default function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState({});

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      if (user?.role === 'ADMIN') {
        const [inv, pending, lowStock] = await Promise.all([
          api.get('/api/inventory?page=0&size=1'),
          api.get('/api/requests/pending'),
          api.get('/api/inventory/low-stock'),
        ]);
        setStats({
          totalItems: inv.data.totalElements,
          pendingRequests: pending.data.length,
          lowStockItems: lowStock.data.length,
        });
      } else {
        const myReqs = await api.get('/api/requests/my');
        const pending = myReqs.data.filter((r) => r.status === 'PENDING').length;
        const approved = myReqs.data.filter((r) => r.status === 'APPROVED').length;
        const rejected = myReqs.data.filter((r) => r.status === 'REJECTED').length;
        setStats({ pending, approved, rejected, total: myReqs.data.length });
      }
    } catch (err) {
      console.error('Failed to fetch stats', err);
    }
  };

  return (
    <div className="page">
      <h2>Welcome, {user?.email} 👋</h2>
      <p className="subtitle">Role: <span className="user-badge">{user?.role}</span></p>

      <div className="stats-grid">
        {user?.role === 'ADMIN' ? (
          <>
            <div className="stat-card stat-blue">
              <div className="stat-icon">📦</div>
              <div className="stat-value">{stats.totalItems ?? '—'}</div>
              <div className="stat-label">Total Items</div>
            </div>
            <div className="stat-card stat-yellow">
              <div className="stat-icon">⏳</div>
              <div className="stat-value">{stats.pendingRequests ?? '—'}</div>
              <div className="stat-label">Pending Requests</div>
            </div>
            <div className="stat-card stat-red">
              <div className="stat-icon">⚠️</div>
              <div className="stat-value">{stats.lowStockItems ?? '—'}</div>
              <div className="stat-label">Low Stock Items</div>
            </div>
          </>
        ) : (
          <>
            <div className="stat-card stat-yellow">
              <div className="stat-icon">⏳</div>
              <div className="stat-value">{stats.pending ?? '—'}</div>
              <div className="stat-label">Pending</div>
            </div>
            <div className="stat-card stat-green">
              <div className="stat-icon">✅</div>
              <div className="stat-value">{stats.approved ?? '—'}</div>
              <div className="stat-label">Approved</div>
            </div>
            <div className="stat-card stat-red">
              <div className="stat-icon">❌</div>
              <div className="stat-value">{stats.rejected ?? '—'}</div>
              <div className="stat-label">Rejected</div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
