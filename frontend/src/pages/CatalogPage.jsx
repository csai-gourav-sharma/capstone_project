import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../api/axiosConfig';

export default function CatalogPage() {
  const [items, setItems] = useState([]);
  const [search, setSearch] = useState('');
  const [cart, setCart] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchItems(); }, []);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/inventory?page=0&size=100');
      setItems(res.data.content || []);
    } catch { toast.error('Failed to load catalog'); }
    finally { setLoading(false); }
  };

  const handleSearch = async () => {
    if (!search.trim()) { fetchItems(); return; }
    try {
      const res = await api.get(`/api/inventory/search?q=${search}`);
      setItems(res.data || []);
    } catch { toast.error('Search failed'); }
  };

  const updateCart = (item, qty) => {
    const q = parseInt(qty) || 0;
    if (q <= 0) {
      const c = { ...cart }; delete c[item.id]; setCart(c);
    } else {
      setCart({ ...cart, [item.id]: { itemId: item.id, name: item.name, quantity: q } });
    }
  };

  const submitRequest = async () => {
    const cartItems = Object.values(cart);
    if (cartItems.length === 0) { toast.warn('Add items to your cart first'); return; }
    try {
      await api.post('/api/requests', {
        items: cartItems.map((c) => ({ itemId: c.itemId, quantity: c.quantity })),
      });
      toast.success('Request submitted successfully!');
      setCart({});
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit request');
    }
  };

  const cartCount = Object.keys(cart).length;

  return (
    <div className="page">
      <div className="page-header">
        <h2>📋 Stationery Catalog</h2>
        {cartCount > 0 && (
          <button onClick={submitRequest} className="btn btn-primary">
            🛒 Submit Request ({cartCount} items)
          </button>
        )}
      </div>

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search items..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch} className="btn btn-secondary">Search</button>
        {search && <button onClick={() => { setSearch(''); fetchItems(); }} className="btn btn-ghost">Clear</button>}
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <div className="items-grid">
          {items.map((item) => (
            <div key={item.id} className={`item-card ${item.lowStock ? 'low-stock' : ''}`}>
              <div className="item-header">
                <span className="item-category">{item.category}</span>
                {item.lowStock && <span className="badge badge-red">Low Stock</span>}
              </div>
              <h3>{item.name}</h3>
              <div className="item-details">
                <span>Available: <strong>{item.availableQuantity}</strong></span>
                {item.unit && <span>Unit: {item.unit}</span>}
              </div>
              <div className="item-actions">
                <input
                  type="number"
                  min="0"
                  max={item.availableQuantity}
                  placeholder="Qty"
                  value={cart[item.id]?.quantity || ''}
                  onChange={(e) => updateCart(item, e.target.value)}
                  className="qty-input"
                />
                {cart[item.id] && <span className="in-cart">✓ In Cart</span>}
              </div>
            </div>
          ))}
          {items.length === 0 && <p className="empty">No items found.</p>}
        </div>
      )}
    </div>
  );
}
