import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Search, ArrowRightLeft, PlaneTakeoff, PlaneLanding, Calendar } from 'lucide-react';
import AirportSearch from './AirportSearch';

const FARE_TYPES = [
  { value: 'REGULAR', label: 'Regular' },
  { value: 'STUDENT', label: 'Student', discount: '10%' },
  { value: 'ARMED_FORCES', label: 'Armed Forces', discount: '15%' },
  { value: 'SENIOR_CITIZEN', label: 'Senior Citizen', discount: '12%' },
  { value: 'MEDICAL', label: 'Doctors & Nurses', discount: '10%' },
];

export default function FlightSearchForm({ compact = false }) {
  const [searchParams] = useSearchParams();
  const [origin, setOrigin] = useState(searchParams?.get('origin') || '');
  const [dest, setDest] = useState(searchParams?.get('dest') || '');
  const [date, setDate] = useState(searchParams?.get('date') || '');
  const [returnDate, setReturnDate] = useState('');
  const [tripType, setTripType] = useState('one_way');
  const [fareType, setFareType] = useState('REGULAR');
  const navigate = useNavigate();

  const today = new Date().toISOString().split('T')[0];

  const swap = () => {
    const temp = origin;
    setOrigin(dest);
    setDest(temp);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (origin && dest && date) {
      let url = `/flights?origin=${origin}&dest=${dest}&date=${date}&fareType=${fareType}&tripType=${tripType}`;
      if (tripType === 'round_trip' && returnDate) url += `&returnDate=${returnDate}`;
      navigate(url);
    }
  };

  return (
    <form onSubmit={handleSearch} className={`bg-white rounded-2xl shadow-xl ${compact ? 'p-4' : 'p-6 md:p-8'} max-w-5xl mx-auto`}>
      {/* Trip type + Fare type row */}
      <div className="flex flex-wrap gap-3 mb-4">
        {/* Trip type toggle */}
        <div className="flex bg-gray-100 rounded-lg p-0.5">
          {[{ v: 'one_way', l: 'One Way' }, { v: 'round_trip', l: 'Round Trip' }].map(t => (
            <button key={t.v} type="button" onClick={() => setTripType(t.v)}
              className={`px-4 py-1.5 rounded-md text-xs font-medium transition cursor-pointer border-none ${
                tripType === t.v ? 'bg-[#1e3a5f] text-white shadow-sm' : 'bg-transparent text-gray-600 hover:text-gray-800'
              }`}>{t.l}</button>
          ))}
        </div>

        {/* Fare type */}
        <div className="flex gap-1.5 flex-wrap">
          {FARE_TYPES.map(f => (
            <button key={f.value} type="button" onClick={() => setFareType(f.value)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition cursor-pointer border ${
                fareType === f.value
                  ? 'bg-[#1e3a5f] text-white border-[#1e3a5f]'
                  : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
              }`}>
              {f.label}
              {f.discount && <span className="ml-1 opacity-70">-{f.discount}</span>}
            </button>
          ))}
        </div>
      </div>

      {/* Search fields */}
      <div className={`grid grid-cols-1 ${tripType === 'round_trip' ? 'md:grid-cols-[1fr,auto,1fr,1fr,1fr,auto]' : 'md:grid-cols-[1fr,auto,1fr,1fr,auto]'} gap-3 items-end`}>

        {/* From */}
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">From</label>
          <AirportSearch value={origin} onChange={setOrigin} placeholder="City or airport code" icon={PlaneTakeoff} excludeCode={dest} />
        </div>

        {/* Swap */}
        <button type="button" onClick={swap}
          className="self-end mb-1 p-2 rounded-full hover:bg-gray-100 transition bg-transparent border border-gray-200 cursor-pointer">
          <ArrowRightLeft className="w-4 h-4 text-gray-500" />
        </button>

        {/* To */}
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">To</label>
          <AirportSearch value={dest} onChange={setDest} placeholder="City or airport code" icon={PlaneLanding} excludeCode={origin} />
        </div>

        {/* Departure date */}
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">Departure</label>
          <div className="relative">
            <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
            <input type="date" value={date} onChange={e => setDate(e.target.value)} min={today} required
              className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:outline-none bg-white text-gray-800 text-sm" />
          </div>
        </div>

        {/* Return date (round trip only) */}
        {tripType === 'round_trip' && (
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">Return</label>
            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
              <input type="date" value={returnDate} onChange={e => setReturnDate(e.target.value)}
                min={date || today} required
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:outline-none bg-white text-gray-800 text-sm" />
            </div>
          </div>
        )}

        {/* Search button */}
        <button type="submit"
          className="bg-[#1e3a5f] text-white px-6 py-3 rounded-lg hover:bg-[#2a4d7a] transition flex items-center justify-center gap-2 font-semibold cursor-pointer border-none text-sm h-[46px]">
          <Search className="w-4 h-4" /> Search
        </button>
      </div>
    </form>
  );
}
