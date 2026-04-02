import { useState, useEffect } from 'react';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { Loader2, Plane, Download, XCircle, RefreshCw } from 'lucide-react';

const statusColors = {
  CONFIRMED: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchBookings = () => {
    setLoading(true);
    api.get('/bookings').then(r => setBookings(r.data)).catch(() => toast.error('Failed to load bookings')).finally(() => setLoading(false));
  };

  useEffect(() => { fetchBookings(); }, []);

  const handleCancel = async (id) => {
    if (!confirm('Cancel this booking? A 25% penalty will be applied.')) return;
    try {
      await api.delete(`/bookings/${id}`);
      toast.success('Booking cancelled');
      fetchBookings();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cancellation failed');
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#1e3a5f]">My Bookings</h1>
        <button onClick={fetchBookings} disabled={loading}
          className="flex items-center gap-1.5 text-sm text-[#1e3a5f] font-medium hover:underline bg-transparent border-none cursor-pointer disabled:opacity-50">
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} /> Refresh
        </button>
      </div>

      {bookings.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-xl shadow-sm">
          <Plane className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">No bookings yet. Search for a flight to get started!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map(b => (
            <div key={b.id} className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                  <Plane className="w-5 h-5 text-[#1e3a5f]" />
                  <span className="font-bold text-[#1e3a5f]">{b.flightNumber}</span>
                  <span className={`text-xs px-2 py-1 rounded-full font-medium ${statusColors[b.status]}`}>{b.status}</span>
                </div>
                {b.transactionId && <span className="text-xs text-gray-400 font-mono">{b.transactionId}</span>}
              </div>

              <div className="flex items-center justify-between mb-3">
                <div>
                  <p className="text-lg font-semibold">{b.origin} ({b.originCode}) &rarr; {b.destination} ({b.destCode})</p>
                  <p className="text-sm text-gray-500">{new Date(b.departureTime).toLocaleString()} &bull; {b.seatClass} &bull; {b.numSeats} pax</p>
                </div>
                <div className="text-right">
                  <p className="text-xl font-bold text-[#1e3a5f]">{'\u20B9'}{Number(b.totalPrice).toLocaleString('en-IN')}</p>
                  {b.penaltyAmount > 0 && <p className="text-xs text-red-500">Penalty: {'\u20B9'}{Number(b.penaltyAmount).toLocaleString('en-IN')}</p>}
                </div>
              </div>

              {b.passengers && b.passengers.length > 0 && (
                <div className="text-sm text-gray-500 mb-3">
                  {b.passengers.map((p, i) => (
                    <span key={i}>{p.name} (Seat {p.seatNumber || 'TBD'}){i < b.passengers.length - 1 ? ', ' : ''}</span>
                  ))}
                </div>
              )}

              <div className="flex gap-3 pt-3 border-t border-gray-100">
                {b.status === 'CONFIRMED' && (
                  <>
                    <button onClick={async () => {
                        try {
                          const res = await api.get(`/tickets/${b.id}/eticket`, { responseType: 'blob' });
                          const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
                          const a = document.createElement('a');
                          a.href = url;
                          a.download = `SkyWings-ETicket-${b.transactionId || b.id}.pdf`;
                          a.click();
                          window.URL.revokeObjectURL(url);
                        } catch { toast.error('Failed to download e-ticket'); }
                      }}
                      className="flex items-center gap-1 text-sm text-[#1e3a5f] font-semibold hover:underline bg-transparent border-none cursor-pointer">
                      <Download className="w-4 h-4" /> E-Ticket
                    </button>
                    <button onClick={() => handleCancel(b.id)}
                      className="flex items-center gap-1 text-sm text-red-500 font-semibold hover:underline bg-transparent border-none cursor-pointer ml-auto">
                      <XCircle className="w-4 h-4" /> Cancel
                    </button>
                  </>
                )}
                {b.status === 'PENDING' && (
                  <a href={`/payment/${b.id}`} className="text-sm text-[#1e3a5f] font-semibold hover:underline no-underline">Complete Payment &rarr;</a>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
