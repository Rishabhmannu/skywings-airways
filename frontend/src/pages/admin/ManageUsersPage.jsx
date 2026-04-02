import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Loader2, Users, ShieldCheck, ShieldOff, Trash2, RefreshCw } from 'lucide-react';

export default function ManageUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = () => {
    setLoading(true);
    api.get('/admin/users').then(r => setUsers(r.data)).finally(() => setLoading(false));
  };

  useEffect(() => { fetchUsers(); }, []);

  const handleRoleChange = async (id, currentRole, email) => {
    const newRole = currentRole === 'ADMIN' ? 'PASSENGER' : 'ADMIN';
    const action = newRole === 'ADMIN' ? 'Promote to Admin' : 'Demote to Passenger';
    if (!confirm(`${action} for ${email}?`)) return;
    try {
      await api.put(`/admin/users/${id}/role`, { role: newRole });
      toast.success(`${email} is now ${newRole}`);
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update role');
    }
  };

  const handleDelete = async (id, email) => {
    if (!confirm(`Delete user ${email}? This cannot be undone.`)) return;
    try {
      await api.delete(`/admin/users/${id}`);
      toast.success(`User ${email} deleted`);
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete user');
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#1e3a5f] flex items-center gap-2"><Users className="w-6 h-6" /> Registered Users</h1>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-400">{users.length} user{users.length !== 1 ? 's' : ''}</span>
          <button onClick={fetchUsers} disabled={loading}
            className="flex items-center gap-1.5 text-sm text-[#1e3a5f] font-medium hover:underline bg-transparent border-none cursor-pointer">
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600">
            <tr>
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-left">Name</th>
              <th className="px-4 py-3 text-left">Email</th>
              <th className="px-4 py-3 text-left">Phone</th>
              <th className="px-4 py-3 text-left">Role</th>
              <th className="px-4 py-3 text-left">Joined</th>
              <th className="px-4 py-3 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {users.map(u => (
              <tr key={u.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs">{u.id}</td>
                <td className="px-4 py-3 font-semibold">{u.name}</td>
                <td className="px-4 py-3">{u.email}</td>
                <td className="px-4 py-3">{u.phone}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs px-2 py-1 rounded-full font-medium ${u.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}`}>{u.role}</span>
                </td>
                <td className="px-4 py-3 text-xs">{new Date(u.createdAt).toLocaleDateString()}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2 justify-center">
                    <button onClick={() => handleRoleChange(u.id, u.role, u.email)}
                      title={u.role === 'ADMIN' ? 'Demote to Passenger' : 'Promote to Admin'}
                      className={`p-1.5 rounded transition border-none cursor-pointer ${u.role === 'ADMIN' ? 'text-amber-600 hover:bg-amber-50 bg-transparent' : 'text-purple-600 hover:bg-purple-50 bg-transparent'}`}>
                      {u.role === 'ADMIN' ? <ShieldOff className="w-4 h-4" /> : <ShieldCheck className="w-4 h-4" />}
                    </button>
                    <button onClick={() => handleDelete(u.id, u.email)}
                      title="Delete user"
                      className="p-1.5 rounded text-red-500 hover:bg-red-50 transition bg-transparent border-none cursor-pointer">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
