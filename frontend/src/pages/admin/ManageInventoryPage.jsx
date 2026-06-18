import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

const CATEGORIES = ['PAPER', 'PEN', 'PENCIL', 'NOTEBOOK', 'ERASER', 'OTHER'];
const emptyForm = { name: '', category: 'PAPER', unit: '', availableQuantity: 0, minimumQuantity: 10 };

export default function ManageInventoryPage() {
  const [items, setItems] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(emptyForm);

  useEffect(() => { fetchItems(); }, []);

  const fetchItems = async () => {
    try {
      const res = await api.get('/api/inventory?page=0&size=100');
      setItems(res.data.content || []);
    } catch { toast.error('Failed to load inventory'); }
  };

  const openAdd = () => { setEditId(null); setForm(emptyForm); setShowModal(true); };
  const openEdit = (item) => {
    setEditId(item.id);
    setForm({ name: item.name, category: item.category, unit: item.unit || '', availableQuantity: item.availableQuantity, minimumQuantity: item.minimumQuantity || 10 });
    setShowModal(true);
  };

  const handleSave = async () => {
    try {
      if (editId) {
        await api.put(`/api/inventory/${editId}`, form);
        toast.success('Item updated');
      } else {
        await api.post('/api/inventory', form);
        toast.success('Item added');
      }
      setShowModal(false);
      fetchItems();
    } catch (err) { toast.error(err.response?.data?.message || 'Save failed'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this item?')) return;
    try {
      await api.delete(`/api/inventory/${id}`);
      toast.success('Item deleted');
      fetchItems();
    } catch { toast.error('Delete failed'); }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>📦 Manage Inventory</h2>
        <button onClick={openAdd} className="btn btn-primary">+ Add Item</button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th><th>Name</th><th>Category</th><th>Unit</th><th>Qty</th><th>Min</th><th>Status</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className={item.lowStock ? 'row-low-stock' : ''}>
                <td>{item.id}</td>
                <td>{item.name}</td>
                <td><span className="badge badge-blue">{item.category}</span></td>
                <td>{item.unit || '—'}</td>
                <td><strong>{item.availableQuantity}</strong></td>
                <td>{item.minimumQuantity || '—'}</td>
                <td>{item.lowStock ? <span className="badge badge-red">Low Stock</span> : <span className="badge badge-green">OK</span>}</td>
                <td className="action-cell">
                  <button onClick={() => openEdit(item)} className="btn btn-sm btn-secondary">Edit</button>
                  <button onClick={() => handleDelete(item.id)} className="btn btn-sm btn-danger">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>{editId ? 'Edit Item' : 'Add New Item'}</h3>
            <div className="form-group">
              <label>Name</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Category</label>
              <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Unit</label>
                <input value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })} placeholder="e.g. ream, piece" />
              </div>
              <div className="form-group">
                <label>Quantity</label>
                <input type="number" value={form.availableQuantity} onChange={(e) => setForm({ ...form, availableQuantity: parseInt(e.target.value) || 0 })} />
              </div>
              <div className="form-group">
                <label>Min Stock</label>
                <input type="number" value={form.minimumQuantity} onChange={(e) => setForm({ ...form, minimumQuantity: parseInt(e.target.value) || 0 })} />
              </div>
            </div>
            <div className="modal-actions">
              <button onClick={() => setShowModal(false)} className="btn btn-ghost">Cancel</button>
              <button onClick={handleSave} className="btn btn-primary">{editId ? 'Update' : 'Add'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
