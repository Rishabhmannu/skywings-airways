import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, ArrowRightLeft, PlaneTakeoff, PlaneLanding, Calendar } from 'lucide-react';

const airports = [
  { code: 'DEL', city: 'New Delhi' }, { code: 'BOM', city: 'Mumbai' },
  { code: 'BLR', city: 'Bangalore' }, { code: 'MAA', city: 'Chennai' },
  { code: 'CCU', city: 'Kolkata' }, { code: 'HYD', city: 'Hyderabad' },
  { code: 'GOI', city: 'Goa' }, { code: 'JAI', city: 'Jaipur' },
  { code: 'DXB', city: 'Dubai' }, { code: 'SIN', city: 'Singapore' },
  { code: 'LHR', city: 'London' }, { code: 'BKK', city: 'Bangkok' },
  { code: 'JFK', city: 'New York' },
];

export default function FlightSearchForm({ compact = false }) {
  const [origin, setOrigin] = useState('');
  const [dest, setDest] = useState('');
  const [date, setDate] = useState('');
  const navigate = useNavigate();

  const today = new Date().toISOString().split('T')[0];

  const swap = () => {
    setOrigin(dest);
    setDest(origin);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (origin && dest && date) {
      navigate(`/flights?origin=${origin}&dest=${dest}&date=${date}`);
    }
  };

  const selectClass = "w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:outline-none bg-white text-gray-800 appearance-auto cursor-pointer text-sm";
  const inputClass = "w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:outline-none bg-white text-gray-800 text-sm";

  return (
    <form onSubmit={handleSearch} className={`bg-white rounded-2xl shadow-xl ${compact ? 'p-4' : 'p-6 md:p-8'} max-w-4xl mx-auto`}>
      <div className={`grid grid-cols-1 ${compact ? 'md:grid-cols-4' : 'md:grid-cols-[1fr,auto,1fr,1fr,auto]'} gap-4 items-end`}>

        {/* From */}
        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1.5">From</label>
          <div className="relative">
            <PlaneTakeoff className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
            <select value={origin} onChange={e => setOrigin(e.target.value)} required className={selectClass}>
              <option value="">Select origin</option>
              {airports.map(a => (
                <option key={a.code} value={a.code} disabled={a.code === dest}>
                  {a.city} ({a.code})
                </option>
              ))}
            </select>
          </div>
          {origin && <p className="text-xs text-blue-600 mt-1 font-medium">{airports.find(a => a.code === origin)?.city} ({origin})</p>}
        </div>

        {/* Swap button */}
        {!compact && (
          <button type="button" onClick={swap}
            className="self-end mb-2 p-2 rounded-full hover:bg-gray-100 transition bg-transparent border border-gray-200 cursor-pointer">
            <ArrowRightLeft className="w-4 h-4 text-gray-500" />
          </button>
        )}

        {/* To */}
        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1.5">To</label>
          <div className="relative">
            <PlaneLanding className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
            <select value={dest} onChange={e => setDest(e.target.value)} required className={selectClass}>
              <option value="">Select destination</option>
              {airports.map(a => (
                <option key={a.code} value={a.code} disabled={a.code === origin}>
                  {a.city} ({a.code})
                </option>
              ))}
            </select>
          </div>
          {dest && <p className="text-xs text-blue-600 mt-1 font-medium">{airports.find(a => a.code === dest)?.city} ({dest})</p>}
        </div>

        {/* Date */}
        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1.5">Departure</label>
          <div className="relative">
            <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
            <input type="date" value={date} onChange={e => setDate(e.target.value)} min={today} required
              className={inputClass} />
          </div>
          {date && <p className="text-xs text-blue-600 mt-1 font-medium">{new Date(date + 'T00:00').toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })}</p>}
        </div>

        {/* Search button */}
        <button type="submit"
          className="bg-[#1e3a5f] text-white px-6 py-3 rounded-lg hover:bg-[#2a4d7a] transition flex items-center justify-center gap-2 font-semibold cursor-pointer border-none text-sm h-[46px]">
          <Search className="w-4 h-4" /> Search Flights
        </button>
      </div>
    </form>
  );
}
