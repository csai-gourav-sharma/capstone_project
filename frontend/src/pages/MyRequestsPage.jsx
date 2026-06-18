import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../api/axiosConfig';

export default function MyRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchRequests(); }, []);

  const fetchRequests = async () => {
    try {
      const res = await api.get('/api/requests/my');
      setRequests(res.data || []);
    } catch { toast.error('Failed to load requests'); }
    finally { setLoading(false); }
  };

  const statusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'badge-yellow';
      case 'APPROVED': return 'badge-green';
      case 'REJECTED': return 'badge-red';
      default: return 'badge-gray';
    }
  };

  return (
    <div className="page">
      <h2>📄 My Requests</h2>
      {loading ? <div className="loading">Loading...</div> : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Date</th>
                <th>Items</th>
                <th>Status</th>
                <th>Admin Comment</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((req) => (
                <tr key={req.id}>
                  <td>{req.id}</td>
                  <td>{new Date(req.requestDate).toLocaleDateString()}</td>
                  <td>
                    <ul className="item-list">
                      {req.items.map((item, i) => (
                        <li key={i}>{item.itemName} × {item.quantity}</li>
                      ))}
                    </ul>
                  </td>
                  <td><span className={`badge ${statusColor(req.status)}`}>{req.status}</span></td>
                  <td>{req.adminComment || '—'}</td>
                </tr>
              ))}
              {requests.length === 0 && (
                <tr><td colSpan="5" className="empty">No requests yet. Browse the catalog to submit one!</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
