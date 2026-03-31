import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/axios';
import { Loader2, Plane, BookOpen, Users, IndianRupee } from 'lucide-react';

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/admin/dashboard').then(r => setStats(r.data)).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  const cards = [
    { label: 'Total Flights', value: stats?.totalFlights, icon: <Plane className="w-6 h-6" />, color: 'bg-blue-50 text-blue-600', link: '/admin/flights' },
    { label: 'Total Bookings', value: stats?.totalBookings, icon: <BookOpen className="w-6 h-6" />, color: 'bg-green-50 text-green-600', link: '/admin/bookings' },
    { label: 'Registered Users', value: stats?.totalUsers, icon: <Users className="w-6 h-6" />, color: 'bg-purple-50 text-purple-600', link: '/admin/users' },
    { label: 'Revenue (Simulated)', value: `\u20B9${Number(stats?.totalRevenue || 0).toLocaleString('en-IN')}`, icon: <IndianRupee className="w-6 h-6" />, color: 'bg-amber-50 text-amber-600' },
  ];

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#1e3a5f] mb-6">Admin Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {cards.map((c, i) => (
          <div key={i} className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
            <div className={`inline-flex items-center justify-center w-12 h-12 rounded-lg mb-3 ${c.color}`}>{c.icon}</div>
            <p className="text-sm text-gray-500">{c.label}</p>
            <p className="text-2xl font-bold text-gray-800">{c.value}</p>
            {c.link && <Link to={c.link} className="text-xs text-[#1e3a5f] hover:underline mt-2 inline-block">View all &rarr;</Link>}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
          <p className="text-sm text-gray-500">Confirmed Bookings</p>
          <p className="text-3xl font-bold text-green-600">{stats?.confirmedBookings}</p>
        </div>
        <div className="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
          <p className="text-sm text-gray-500">Cancelled Bookings</p>
          <p className="text-3xl font-bold text-red-500">{stats?.cancelledBookings}</p>
        </div>
      </div>
    </div>
  );
}
