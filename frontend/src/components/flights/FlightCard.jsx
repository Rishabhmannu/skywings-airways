import { useNavigate } from 'react-router-dom';
import { Plane, Clock, ArrowRight } from 'lucide-react';

export default function FlightCard({ flight, source }) {
  const navigate = useNavigate();
  const isLive = source === 'AMADEUS';

  const formatTime = (dt) => {
    if (!dt) return '--:--';
    const d = new Date(dt);
    return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false });
  };

  const formatDate = (dt) => {
    if (!dt) return '';
    const d = new Date(dt);
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  };

  const origin = flight.originCode || flight.origin;
  const dest = flight.destCode || flight.destination;
  const depTime = flight.departureTime;
  const arrTime = flight.arrivalTime;

  return (
    <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition p-6 border border-gray-100">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Plane className="w-5 h-5 text-[#1e3a5f]" />
          <span className="font-bold text-[#1e3a5f]">{flight.flightNumber || flight.airline}</span>
          {isLive && (
            <span className="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full font-medium">LIVE</span>
          )}
          {!isLive && (
            <span className="text-gray-400 text-xs">{flight.airline}</span>
          )}
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-[#1e3a5f]">
            {flight.currency === 'INR' || !flight.currency ? '\u20B9' : '$'}{Number(flight.price || flight.basePriceEconomy).toLocaleString('en-IN')}
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

      {!isLive && flight.id && (
        <div className="flex justify-between items-center pt-4 border-t border-gray-100">
          <div className="flex gap-4 text-sm">
            <span className="text-gray-500">Economy: <strong>{flight.availableEconomySeats}</strong> seats</span>
            <span className="text-gray-500">Business: <strong>{flight.availableBusinessSeats}</strong> seats</span>
          </div>
          <button onClick={() => navigate(`/booking/${flight.id}`)}
            className="bg-[#1e3a5f] text-white px-5 py-2 rounded-lg hover:bg-[#2a4d7a] transition font-semibold text-sm cursor-pointer border-none">
            Book Now
          </button>
        </div>
      )}

      {isLive && (
        <div className="pt-4 border-t border-gray-100 text-center">
          <p className="text-xs text-gray-400">Real-time data from Amadeus API &mdash; book via SkyWings flights for full experience</p>
        </div>
      )}
    </div>
  );
}
