import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

export default function ManageRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [filter, setFilter] = useState('pending');
  const [rejectId, setRejectId] = useState(null);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchRequests(); }, [filter]);

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const endpoint = filter === 'pending' ? '/api/requests/pending' : '/api/requests/all';
      const res = await api.get(endpoint);
      let data = res.data || [];
      if (filter !== 'pending' && filter !== 'all') {
        data = data.filter((r) => r.status === filter.toUpperCase());
      }
      setRequests(data);
    } catch { toast.error('Failed to load requests'); }
    finally { setLoading(false); }
  };

  const handleApprove = async (id) => {
    try {
      await api.put(`/api/requests/${id}/approve`);
      toast.success(`Request #${id} approved! Stock deducted.`);
      fetchRequests();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Approve failed');
    }
  };

  const handleReject = async () => {
    try {
      await api.put(`/api/requests/${rejectId}/reject`, { comment });
      toast.success(`Request #${rejectId} rejected`);
      setRejectId(null);
      setComment('');
      fetchRequests();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Reject failed');
    }
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
      <div className="page-header">
        <h2>📝 Manage Requests</h2>
        <div className="filter-tabs">
          {['pending', 'all', 'approved', 'rejected'].map((f) => (
            <button key={f} onClick={() => setFilter(f)}
              className={`btn btn-tab ${filter === f ? 'active' : ''}`}>
              {f.charAt(0).toUpperCase() + f.slice(1)}
            </button>
          ))}
        </div>
      </div>

      {loading ? <div className="loading">Loading...</div> : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>#</th><th>Student</th><th>Date</th><th>Items</th><th>Status</th><th>Comment</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((req) => (
                <tr key={req.id}>
                  <td>{req.id}</td>
                  <td>{req.studentEmail}</td>
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
                  <td className="action-cell">
                    {req.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleApprove(req.id)} className="btn btn-sm btn-success">Approve</button>
                        <button onClick={() => setRejectId(req.id)} className="btn btn-sm btn-danger">Reject</button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
              {requests.length === 0 && (
                <tr><td colSpan="7" className="empty">No requests found.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {rejectId && (
        <div className="modal-overlay" onClick={() => setRejectId(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Reject Request #{rejectId}</h3>
            <div className="form-group">
              <label>Rejection Comment</label>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Reason for rejection..."
                rows={3}
              />
            </div>
            <div className="modal-actions">
              <button onClick={() => setRejectId(null)} className="btn btn-ghost">Cancel</button>
              <button onClick={handleReject} className="btn btn-danger">Reject Request</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
