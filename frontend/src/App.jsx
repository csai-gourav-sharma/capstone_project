import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CatalogPage from './pages/CatalogPage';
import MyRequestsPage from './pages/MyRequestsPage';
import ManageInventoryPage from './pages/admin/ManageInventoryPage';
import ManageRequestsPage from './pages/admin/ManageRequestsPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="main-content">
          <Routes>
            {/* Public */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Protected — any role */}
            <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/catalog" element={<ProtectedRoute><CatalogPage /></ProtectedRoute>} />
            <Route path="/my-requests" element={<ProtectedRoute><MyRequestsPage /></ProtectedRoute>} />

            {/* Protected — ADMIN only */}
            <Route path="/admin/inventory" element={<ProtectedRoute adminOnly><ManageInventoryPage /></ProtectedRoute>} />
            <Route path="/admin/requests" element={<ProtectedRoute adminOnly><ManageRequestsPage /></ProtectedRoute>} />

            {/* Default redirect */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
        <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} theme="dark" />
      </BrowserRouter>
    </AuthProvider>
  );
}
