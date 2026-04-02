import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Loader2, BookOpen, ChevronDown, ChevronUp, XCircle, Download } from 'lucide-react';

const statusColors = {
  CONFIRMED: 'bg-green-100 text-green-700',
  PENDING: 'bg-yellow-100 text-yellow-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function ManageBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [filter, setFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  const fetchBookings = () => {
    const url = filter ? `/admin/bookings?status=${filter}` : '/admin/bookings';
    setLoading(true);
    api.get(url).then(r => setBookings(r.data)).finally(() => setLoading(false));
  };

  useEffect(() => { fetchBookings(); }, [filter]);

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

  const handleDownloadTicket = async (bookingId, txnId) => {
    try {
      const res = await api.get(`/tickets/${bookingId}/eticket`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const a = document.createElement('a');
      a.href = url;
      a.download = `SkyWings-ETicket-${txnId || bookingId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Failed to download e-ticket');
    }
  };

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
        <span className="ml-auto text-sm text-gray-400 self-center">{bookings.length} booking{bookings.length !== 1 ? 's' : ''}</span>
      </div>

      <div className="space-y-3">
        {bookings.map(b => (
          <div key={b.id} className="bg-white rounded-xl shadow-sm border border-gray-100">
            {/* Main row */}
            <div className="flex items-center gap-4 px-5 py-4 cursor-pointer hover:bg-gray-50 transition"
              onClick={() => setExpandedId(expandedId === b.id ? null : b.id)}>
              <span className="font-mono text-xs text-gray-400 w-8">#{b.id}</span>
              <span className="font-bold text-[#1e3a5f] w-24">{b.flightNumber}</span>
              <span className="text-sm w-28">{b.originCode} &rarr; {b.destCode}</span>
              <span className="text-sm w-20">{b.seatClass}</span>
              <span className="text-sm w-12 text-center">{b.numSeats} pax</span>
              <span className="text-sm font-semibold w-28 text-right">{'\u20B9'}{Number(b.totalPrice).toLocaleString('en-IN')}</span>
              <span className={`text-xs px-2 py-1 rounded-full font-medium w-24 text-center ${statusColors[b.status]}`}>{b.status}</span>
              <span className="text-xs text-gray-400 w-24">{new Date(b.bookingDate).toLocaleDateString()}</span>
              <span className="ml-auto text-gray-400">
                {expandedId === b.id ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </span>
            </div>

            {/* Expanded details */}
            {expandedId === b.id && (
              <div className="border-t border-gray-100 px-5 py-4 bg-gray-50">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Flight & Booking Info */}
                  <div>
                    <h4 className="text-xs font-semibold text-gray-500 mb-2 uppercase">Booking Details</h4>
                    <div className="text-sm space-y-1">
                      <p><span className="text-gray-500">Transaction:</span> <span className="font-mono">{b.transactionId || '—'}</span></p>
                      <p><span className="text-gray-500">Flight:</span> {b.flightNumber} ({b.flightType})</p>
                      <p><span className="text-gray-500">Route:</span> {b.origin} ({b.originCode}) &rarr; {b.destination} ({b.destCode})</p>
                      <p><span className="text-gray-500">Departure:</span> {new Date(b.departureTime).toLocaleString()}</p>
                      <p><span className="text-gray-500">Arrival:</span> {new Date(b.arrivalTime).toLocaleString()}</p>
                      <p><span className="text-gray-500">Fare Type:</span> {b.fareType || 'REGULAR'}</p>
                      <p><span className="text-gray-500">Tax:</span> {'\u20B9'}{Number(b.taxAmount).toLocaleString('en-IN')}</p>
                      {b.penaltyAmount > 0 && <p className="text-red-600"><span className="text-gray-500">Penalty:</span> {'\u20B9'}{Number(b.penaltyAmount).toLocaleString('en-IN')}</p>}
                      <p><span className="text-gray-500">Payment:</span> {b.paymentStatus || '—'}</p>
                    </div>
                  </div>

                  {/* Passengers */}
                  <div>
                    <h4 className="text-xs font-semibold text-gray-500 mb-2 uppercase">Passengers ({b.passengers?.length || 0})</h4>
                    {b.passengers && b.passengers.length > 0 ? (
                      <div className="space-y-2">
                        {b.passengers.map((p, i) => (
                          <div key={i} className="bg-white rounded-lg p-3 border border-gray-200 text-sm">
                            <p className="font-semibold">{p.name} {p.gender && `(${p.gender})`} {p.isSeniorCitizen && <span className="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded ml-1">Senior</span>}</p>
                            <p className="text-xs text-gray-500">
                              Age: {p.age} &bull; Seat: {p.seatNumber || 'TBD'}
                              {p.passportNumber && ` \u00B7 Passport: ${p.passportNumber}`}
                              {p.nationality && ` \u00B7 ${p.nationality}`}
                              {p.mealPreference && p.mealPreference !== 'NO_PREFERENCE' && ` \u00B7 Meal: ${p.mealPreference}`}
                              {p.specialAssistance && p.specialAssistance !== 'NONE' && ` \u00B7 ${p.specialAssistance.replace(/_/g, ' ')}`}
                            </p>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm text-gray-400">No passenger details available</p>
                    )}
                  </div>
                </div>

                {/* Actions */}
                <div className="flex gap-3 mt-4 pt-4 border-t border-gray-200">
                  {b.status === 'CONFIRMED' && (
                    <>
                      <button onClick={() => handleDownloadTicket(b.id, b.transactionId)}
                        className="flex items-center gap-1 text-sm text-[#1e3a5f] font-semibold hover:underline bg-transparent border-none cursor-pointer">
                        <Download className="w-4 h-4" /> Download E-Ticket
                      </button>
                      <button onClick={() => handleCancel(b.id)}
                        className="flex items-center gap-1 text-sm text-red-500 font-semibold hover:underline bg-transparent border-none cursor-pointer ml-auto">
                        <XCircle className="w-4 h-4" /> Cancel Booking
                      </button>
                    </>
                  )}
                  {b.status === 'PENDING' && (
                    <button onClick={() => handleCancel(b.id)}
                      className="flex items-center gap-1 text-sm text-red-500 font-semibold hover:underline bg-transparent border-none cursor-pointer">
                      <XCircle className="w-4 h-4" /> Cancel Booking
                    </button>
                  )}
                  {b.status === 'CANCELLED' && (
                    <span className="text-sm text-gray-400">This booking has been cancelled</span>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
        {bookings.length === 0 && (
          <div className="text-center py-12 bg-white rounded-xl shadow-sm">
            <BookOpen className="w-10 h-10 text-gray-300 mx-auto mb-2" />
            <p className="text-gray-400">No bookings found</p>
          </div>
        )}
      </div>
    </div>
  );
}
