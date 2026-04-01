import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { UserPlus } from 'lucide-react';

export default function SignupPage() {
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '', confirm: '' });
  const [loading, setLoading] = useState(false);
  const { signup } = useAuth();
  const navigate = useNavigate();

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirm) { toast.error('Passwords do not match'); return; }

    setLoading(true);
    try {
      await signup(form.name, form.email, form.password, form.phone);
      toast.success('Account created! Please verify your email.');
      navigate('/verify-email');
    } catch (err) {
      const msg = err.response?.data?.fieldErrors
        ? err.response.data.fieldErrors.map(e => e.message).join(', ')
        : err.response?.data?.message || 'Signup failed';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-8">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-6">
          <UserPlus className="w-12 h-12 text-[#1e3a5f] mx-auto mb-2" />
          <h1 className="text-2xl font-bold text-[#1e3a5f]">Create Account</h1>
          <p className="text-gray-500 text-sm">Join SkyWings Airways</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Full Name</label>
            <input type="text" value={form.name} onChange={update('name')} required minLength={2}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Email</label>
            <input type="email" value={form.email} onChange={update('email')} required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Phone</label>
            <input type="tel" value={form.phone} onChange={update('phone')} required placeholder="+91XXXXXXXXXX"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Password</label>
            <input type="password" value={form.password} onChange={update('password')} required minLength={8}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
            <p className="text-xs text-gray-400 mt-1">Min 8 chars: uppercase, lowercase, digit, special char</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Confirm Password</label>
            <input type="password" value={form.confirm} onChange={update('confirm')} required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </div>
          <button type="submit" disabled={loading}
            className="w-full bg-[#1e3a5f] text-white py-3 rounded-lg hover:bg-[#2a4d7a] transition font-semibold disabled:opacity-50 cursor-pointer border-none">
            {loading ? 'Creating...' : 'Create Account'}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-[#1e3a5f] font-semibold hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
