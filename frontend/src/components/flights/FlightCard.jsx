import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Plane, Clock, ArrowRight, Loader2 } from 'lucide-react';

export default function FlightCard({ flight, source }) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [booking, setBooking] = useState(false);
  const isLive = source === 'GOOGLE_FLIGHTS';

  const formatTime = (dt) => {
    if (!dt) return '--:--';
    // Handle both "2026-04-07T08:00" and "08:30" formats
    if (dt.includes('T') || dt.includes('-')) {
      const d = new Date(dt);
      if (!isNaN(d)) return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false });
    }
    return dt; // Already a time string like "08:30"
  };

  const formatDate = (dt) => {
    if (!dt) return '';
    if (dt.includes('T') || dt.includes('-')) {
      const d = new Date(dt);
      if (!isNaN(d)) return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
    }
    return '';
  };

  const origin = flight.originCode || flight.origin;
  const dest = flight.destCode || flight.destination;
  const depTime = flight.departureTime;
  const arrTime = flight.arrivalTime;
  const price = flight.price || flight.basePriceEconomy;

  const handleBook = async () => {
    if (!user) { navigate('/login'); return; }
    if (!user.emailVerified && user.role !== 'ADMIN') { navigate('/verify-email'); return; }

    // If it's a DB flight with an id, go directly to booking
    if (flight.id) {
      navigate(`/booking/${flight.id}`);
      return;
    }

    // For live flights, import into DB first then navigate
    setBooking(true);
    try {
      const { data } = await api.post('/flights/import-live', flight);
      toast.success('Flight selected! Choose your seats.');
      navigate(`/booking/${data.id}`);
    } catch (err) {
      console.error('Import flight error:', err.response?.status, err.response?.data, err.message);
      if (err.response?.status === 403 || err.response?.status === 401) {
        toast.error('Please log in to book a flight.');
        navigate('/login');
      } else {
        toast.error(err.response?.data?.message || 'Failed to select flight. Please try again.');
      }
    } finally {
      setBooking(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition p-6 border border-gray-100">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Plane className="w-5 h-5 text-[#1e3a5f]" />
          <span className="font-bold text-[#1e3a5f]">{flight.flightNumber || flight.airline}</span>
          {isLive && (
            <span className="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full font-medium">LIVE</span>
          )}
          {!isLive && flight.airline && (
            <span className="text-gray-400 text-xs">{flight.airline}</span>
          )}
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-[#1e3a5f]">
            {flight.currency === 'INR' || !flight.currency ? '\u20B9' : '$'}{Number(price).toLocaleString('en-IN')}
          </p>
          <p className="text-xs text-gray-400">per person</p>
        </div>
      </div>

      <div className="flex items-center justify-between mb-4">
        <div className="text-center">
          <p className="text-2xl font-bold">{formatTime(depTime)}</p>
          <p className="text-sm text-gray-500">{origin}</p>
          <p className="text-xs text-gray-400">{formatDate(depTime)}</p>
        </div>

        <div className="flex-1 flex flex-col items-center mx-4">
          <div className="flex items-center gap-1 text-xs text-gray-400 mb-1">
            <Clock className="w-3 h-3" />
            {flight.duration || '--'}
          </div>
          <div className="w-full flex items-center">
            <div className="flex-1 border-t border-dashed border-gray-300"></div>
            <ArrowRight className="w-4 h-4 text-gray-400 mx-1" />
          </div>
          {flight.stops !== undefined && (
            <p className="text-xs text-gray-400 mt-1">
              {flight.stops === 0 ? 'Direct' : `${flight.stops} stop${flight.stops > 1 ? 's' : ''}`}
            </p>
          )}
        </div>

        <div className="text-center">
          <p className="text-2xl font-bold">{formatTime(arrTime)}</p>
          <p className="text-sm text-gray-500">{dest}</p>
          <p className="text-xs text-gray-400">{formatDate(arrTime)}</p>
        </div>
      </div>

      <div className="flex justify-between items-center pt-4 border-t border-gray-100">
        <div className="flex gap-4 text-sm">
          {!isLive && flight.availableEconomySeats !== undefined && (
            <>
              <span className="text-gray-500">Economy: <strong>{flight.availableEconomySeats}</strong></span>
              <span className="text-gray-500">Business: <strong>{flight.availableBusinessSeats}</strong></span>
            </>
          )}
          {isLive && (
            <span className="text-xs text-gray-400">Powered by Google Flights</span>
          )}
        </div>
        <button onClick={handleBook} disabled={booking}
          className="bg-[#1e3a5f] text-white px-5 py-2 rounded-lg hover:bg-[#2a4d7a] transition font-semibold text-sm cursor-pointer border-none disabled:opacity-50 flex items-center gap-2">
          {booking ? <><Loader2 className="w-4 h-4 animate-spin" /> Selecting...</> : 'Book Now'}
        </button>
      </div>
    </div>
  );
}
