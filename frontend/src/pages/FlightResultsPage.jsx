import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../api/axios';
import FlightCard from '../components/flights/FlightCard';
import FlightSearchForm from '../components/flights/FlightSearchForm';
import { Loader2, AlertCircle, Plane, Tag } from 'lucide-react';

const FARE_LABELS = {
  REGULAR: null,
  STUDENT: { label: 'Student Fare', discount: '10% off', color: 'bg-blue-100 text-blue-700' },
  ARMED_FORCES: { label: 'Armed Forces', discount: '15% off', color: 'bg-green-100 text-green-700' },
  SENIOR_CITIZEN: { label: 'Senior Citizen', discount: '12% off', color: 'bg-amber-100 text-amber-700' },
  MEDICAL: { label: 'Doctors & Nurses', discount: '10% off', color: 'bg-purple-100 text-purple-700' },
};

export default function FlightResultsPage() {
  const [searchParams] = useSearchParams();
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [source, setSource] = useState('');

  const origin = searchParams.get('origin');
  const dest = searchParams.get('dest');
  const date = searchParams.get('date');
  const fareType = searchParams.get('fareType') || 'REGULAR';
  const tripType = searchParams.get('tripType') || 'one_way';
  const returnDate = searchParams.get('returnDate');

  useEffect(() => {
    if (!origin || !dest || !date) return;
    fetchFlights();
  }, [origin, dest, date, tripType, returnDate]);

  const fetchFlights = () => {
    if (!origin || !dest || !date) return;
    setLoading(true);
    setFlights([]);
    setError(false);

    let url = `/flights/live-search?origin=${origin}&dest=${dest}&date=${date}&adults=1`;
    if (tripType === 'round_trip' && returnDate) {
      url += `&tripType=round_trip&returnDate=${returnDate}`;
    }

    api.get(url)
      .then(r => {
        setFlights(r.data);
        setSource(r.data.length > 0 ? r.data[0].source : '');
      })
      .catch(() => { setFlights([]); setError(true); })
      .finally(() => setLoading(false));
  };

  const fareInfo = FARE_LABELS[fareType];

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <FlightSearchForm compact />

      <div className="mt-8">
        {origin && dest && date && (
          <div className="flex flex-wrap items-center gap-3 mb-6">
            <Plane className="w-5 h-5 text-[#1e3a5f]" />
            <h2 className="text-lg font-semibold text-gray-800">
              {origin} &rarr; {dest}
              {tripType === 'round_trip' && returnDate && <span className="text-gray-400 font-normal text-sm ml-2">(Round Trip)</span>}
            </h2>
            <span className="text-sm text-gray-400">
              {new Date(date + 'T00:00').toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' })}
              {tripType === 'round_trip' && returnDate && (
                <> &mdash; {new Date(returnDate + 'T00:00').toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' })}</>
              )}
            </span>
            {fareInfo && (
              <span className={`text-xs px-3 py-1 rounded-full font-medium flex items-center gap-1 ${fareInfo.color}`}>
                <Tag className="w-3 h-3" /> {fareInfo.label} ({fareInfo.discount})
              </span>
            )}
            {source === 'GOOGLE_FLIGHTS' && (
              <span className="ml-auto bg-green-100 text-green-700 text-xs px-3 py-1 rounded-full font-medium">
                Real-time from Google Flights
              </span>
            )}
          </div>
        )}

        {loading ? (
          <div className="flex flex-col items-center justify-center py-20">
            <Loader2 className="w-10 h-10 animate-spin text-[#1e3a5f] mb-3" />
            <p className="text-gray-500">Searching real flights...</p>
            <p className="text-xs text-gray-400 mt-1">Fetching live data from Google Flights</p>
          </div>
        ) : flights.length > 0 ? (
          <div className="space-y-4">
            {flights.map((f, i) => <FlightCard key={i} flight={f} source={f.source || source} fareType={fareType} />)}
          </div>
        ) : (
          <div className="text-center py-16">
            <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 text-lg">{error ? 'Failed to search flights.' : 'No flights found for this route and date.'}</p>
            <p className="text-sm text-gray-400 mt-2">{error ? 'Please check your connection and try again.' : 'Try different dates or airports.'}</p>
            <button onClick={fetchFlights}
              className="mt-4 px-5 py-2 bg-[#1e3a5f] text-white rounded-lg text-sm font-semibold cursor-pointer border-none hover:bg-[#2a4d7a]">
              {error ? 'Retry Search' : 'Search Again'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
