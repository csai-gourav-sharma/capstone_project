import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      const res = await api.post('/api/auth/register', data);
      login(res.data.token, res.data);
      toast.success('Account created successfully!');
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>📦 Stationery MS</h1>
          <p>Create a new account</p>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              placeholder="John Doe"
              {...register('fullName', { required: 'Full name is required' })}
            />
            {errors.fullName && <span className="error">{errors.fullName.message}</span>}
          </div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="you@example.com"
              {...register('email', { required: 'Email is required' })}
            />
            {errors.email && <span className="error">{errors.email.message}</span>}
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="Min 6 characters"
              {...register('password', { required: 'Password is required', minLength: { value: 6, message: 'Min 6 characters' } })}
            />
            {errors.password && <span className="error">{errors.password.message}</span>}
          </div>
          <div className="form-group">
            <label>Role</label>
            <select {...register('role', { required: 'Role is required' })}>
              <option value="STUDENT">Student</option>
              <option value="ADMIN">Admin</option>
            </select>
            {errors.role && <span className="error">{errors.role.message}</span>}
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>
        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign In</Link>
        </p>
      </div>
    </div>
  );
}
