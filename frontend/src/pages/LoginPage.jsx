import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { LogIn } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await login(email, password);
      if (!user.emailVerified && user.role !== 'ADMIN') {
        toast.success('Please verify your email to continue.');
        navigate('/verify-email');
      } else {
        toast.success(`Welcome back, ${user.name}!`);
        navigate(user.role === 'ADMIN' ? '/admin' : '/');
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-6">
          <LogIn className="w-12 h-12 text-[#1e3a5f] mx-auto mb-2" />
          <h1 className="text-2xl font-bold text-[#1e3a5f]">Welcome Back</h1>
          <p className="text-gray-500 text-sm">Sign in to your SkyWings account</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" placeholder="you@example.com" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" placeholder="Enter password" />
          </div>
          <button type="submit" disabled={loading}
            className="w-full bg-[#1e3a5f] text-white py-3 rounded-lg hover:bg-[#2a4d7a] transition font-semibold disabled:opacity-50 cursor-pointer border-none">
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{' '}
          <Link to="/signup" className="text-[#1e3a5f] font-semibold hover:underline">Sign up</Link>
        </p>
      </div>
    </div>
  );
}
