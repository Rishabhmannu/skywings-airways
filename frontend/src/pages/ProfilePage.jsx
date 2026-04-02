import { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { User, Loader2 } from 'lucide-react';

export default function ProfilePage() {
  const { user: authUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/users/profile').then(r => {
      setProfile(r.data);
      setName(r.data.name);
      setPhone(r.data.phone);
    }).finally(() => setLoading(false));
  }, []);

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const { data } = await api.put('/users/profile', { name, phone });
      setProfile(data);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    }
  };

  const handlePassword = async (e) => {
    e.preventDefault();
    try {
      await api.put('/users/change-password', { currentPassword, newPassword });
      toast.success('Password changed');
      setCurrentPassword('');
      setNewPassword('');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Password change failed');
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#1e3a5f] mb-6 flex items-center gap-2"><User className="w-6 h-6" /> Profile</h1>

      <div className="bg-white rounded-xl shadow-md p-6 mb-6 border border-gray-100">
        <h2 className="font-semibold mb-4">Account Details</h2>
        <form onSubmit={handleUpdate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Name</label>
            <input value={name} onChange={e => setName(e.target.value)} required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Email</label>
            <input value={profile?.email || ''} disabled
              className="w-full px-4 py-3 border border-gray-200 rounded-lg bg-gray-50 text-gray-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Phone</label>
            <input value={phone} onChange={e => setPhone(e.target.value)} required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          </div>
          <button type="submit" className="bg-[#1e3a5f] text-white px-6 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-[#2a4d7a]">Save Changes</button>
        </form>
      </div>

      <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
        <h2 className="font-semibold mb-4">Change Password</h2>
        <form onSubmit={handlePassword} className="space-y-4">
          <input type="password" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} required placeholder="Current password"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
          <div>
            <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} required placeholder="New password" minLength={8}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
            <div className="mt-2 text-xs text-gray-400 space-y-0.5">
              <p className={newPassword.length >= 8 ? 'text-green-500' : ''}>
                {newPassword.length >= 8 ? '\u2713' : '\u2022'} At least 8 characters
              </p>
              <p className={/[A-Z]/.test(newPassword) ? 'text-green-500' : ''}>
                {/[A-Z]/.test(newPassword) ? '\u2713' : '\u2022'} One uppercase letter
              </p>
              <p className={/[a-z]/.test(newPassword) ? 'text-green-500' : ''}>
                {/[a-z]/.test(newPassword) ? '\u2713' : '\u2022'} One lowercase letter
              </p>
              <p className={/\d/.test(newPassword) ? 'text-green-500' : ''}>
                {/\d/.test(newPassword) ? '\u2713' : '\u2022'} One digit
              </p>
              <p className={/[@$!%*?&#]/.test(newPassword) ? 'text-green-500' : ''}>
                {/[@$!%*?&#]/.test(newPassword) ? '\u2713' : '\u2022'} One special character (@$!%*?&#)
              </p>
            </div>
          </div>
          <button type="submit" className="bg-[#1e3a5f] text-white px-6 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-[#2a4d7a]">Change Password</button>
        </form>
      </div>
    </div>
  );
}
