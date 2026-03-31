import { useState, useEffect } from 'react';
import api from '../../api/axios';
import { Loader2, BookOpen } from 'lucide-react';

const statusColors = {
  CONFIRMED: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function ManageBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [filter, setFilter] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const url = filter ? `/admin/bookings?status=${filter}` : '/admin/bookings';
    setLoading(true);
    api.get(url).then(r => setBookings(r.data)).finally(() => setLoading(false));
  }, [filter]);

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#1e3a5f] mb-6 flex items-center gap-2"><BookOpen className="w-6 h-6" /> All Bookings</h1>

      <div className="flex gap-2 mb-6">
        {['', 'CONFIRMED', 'PENDING', 'CANCELLED'].map(s => (
          <button key={s} onClick={() => setFilter(s)}
            className={`px-4 py-2 rounded-lg text-sm font-medium cursor-pointer border-none transition ${filter === s ? 'bg-[#1e3a5f] text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            {s || 'All'}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600">
            <tr>
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-left">Flight</th>
              <th className="px-4 py-3 text-left">Route</th>
              <th className="px-4 py-3 text-left">Class</th>
              <th className="px-4 py-3 text-right">Pax</th>
              <th className="px-4 py-3 text-right">Total</th>
              <th className="px-4 py-3 text-left">Status</th>
              <th className="px-4 py-3 text-left">Date</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {bookings.map(b => (
              <tr key={b.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs">{b.id}</td>
                <td className="px-4 py-3 font-bold text-[#1e3a5f]">{b.flightNumber}</td>
                <td className="px-4 py-3">{b.originCode} &rarr; {b.destCode}</td>
                <td className="px-4 py-3">{b.seatClass}</td>
                <td className="px-4 py-3 text-right">{b.numSeats}</td>
                <td className="px-4 py-3 text-right font-semibold">{'\u20B9'}{Number(b.totalPrice).toLocaleString('en-IN')}</td>
                <td className="px-4 py-3"><span className={`text-xs px-2 py-1 rounded-full font-medium ${statusColors[b.status]}`}>{b.status}</span></td>
                <td className="px-4 py-3 text-xs">{new Date(b.bookingDate).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {bookings.length === 0 && <p className="text-center text-gray-400 py-8">No bookings found</p>}
      </div>
    </div>
  );
}
