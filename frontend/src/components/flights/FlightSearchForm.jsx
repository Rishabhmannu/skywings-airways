import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, ArrowRightLeft } from 'lucide-react';

const airports = [
  { code: 'DEL', city: 'New Delhi' }, { code: 'BOM', city: 'Mumbai' },
  { code: 'BLR', city: 'Bangalore' }, { code: 'MAA', city: 'Chennai' },
  { code: 'CCU', city: 'Kolkata' }, { code: 'HYD', city: 'Hyderabad' },
  { code: 'GOI', city: 'Goa' }, { code: 'JAI', city: 'Jaipur' },
  { code: 'DXB', city: 'Dubai' }, { code: 'SIN', city: 'Singapore' },
  { code: 'LHR', city: 'London' }, { code: 'BKK', city: 'Bangkok' },
  { code: 'JFK', city: 'New York' },
];

export default function FlightSearchForm() {
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

  return (
    <form onSubmit={handleSearch} className="bg-white rounded-2xl shadow-xl p-6 md:p-8 max-w-4xl mx-auto">
      <div className="grid grid-cols-1 md:grid-cols-[1fr,auto,1fr,1fr,auto] gap-4 items-end">
        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1">From</label>
          <select value={origin} onChange={e => setOrigin(e.target.value)} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white">
            <option value="">Select origin</option>
            {airports.map(a => (
              <option key={a.code} value={a.code} disabled={a.code === dest}>
                {a.city} ({a.code})
              </option>
            ))}
          </select>
        </div>

        <button type="button" onClick={swap} className="self-end mb-1 p-2 rounded-full hover:bg-gray-100 transition bg-transparent border-none cursor-pointer">
          <ArrowRightLeft className="w-5 h-5 text-gray-400" />
        </button>

        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1">To</label>
          <select value={dest} onChange={e => setDest(e.target.value)} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white">
            <option value="">Select destination</option>
            {airports.map(a => (
              <option key={a.code} value={a.code} disabled={a.code === origin}>
                {a.city} ({a.code})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1">Departure</label>
          <input type="date" value={date} onChange={e => setDate(e.target.value)} min={today} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
        </div>

        <button type="submit"
          className="bg-[#1e3a5f] text-white px-6 py-3 rounded-lg hover:bg-[#2a4d7a] transition flex items-center gap-2 font-semibold cursor-pointer border-none">
          <Search className="w-5 h-5" /> Search
        </button>
      </div>
    </form>
  );
}
