import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../api/axios';
import FlightCard from '../components/flights/FlightCard';
import FlightSearchForm from '../components/flights/FlightSearchForm';
import { Loader2, AlertCircle, Plane } from 'lucide-react';

export default function FlightResultsPage() {
  const [searchParams] = useSearchParams();
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [source, setSource] = useState('');

  const origin = searchParams.get('origin');
  const dest = searchParams.get('dest');
  const date = searchParams.get('date');

  useEffect(() => {
    if (!origin || !dest || !date) return;
    setLoading(true);
    setFlights([]);

    // Primary: SerpAPI Google Flights (real data)
    api.get(`/flights/live-search?origin=${origin}&dest=${dest}&date=${date}&adults=1`)
      .then(r => {
        setFlights(r.data);
        setSource(r.data.length > 0 ? r.data[0].source : '');
      })
      .catch(() => setFlights([]))
      .finally(() => setLoading(false));
  }, [origin, dest, date]);

  const airportName = (code) => {
    const map = { DEL: 'New Delhi', BOM: 'Mumbai', BLR: 'Bangalore', MAA: 'Chennai', CCU: 'Kolkata', HYD: 'Hyderabad', GOI: 'Goa', JAI: 'Jaipur', DXB: 'Dubai', SIN: 'Singapore', LHR: 'London', BKK: 'Bangkok', JFK: 'New York' };
    return map[code] || code;
  };

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <FlightSearchForm compact />

      <div className="mt-8">
        {origin && dest && date && (
          <div className="flex items-center gap-3 mb-6">
            <Plane className="w-5 h-5 text-[#1e3a5f]" />
            <h2 className="text-lg font-semibold text-gray-800">
              {airportName(origin)} ({origin}) &rarr; {airportName(dest)} ({dest})
            </h2>
            <span className="text-sm text-gray-400">
              {new Date(date + 'T00:00').toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })}
            </span>
            {source === 'GOOGLE_FLIGHTS' && (
              <span className="ml-auto bg-green-100 text-green-700 text-xs px-3 py-1 rounded-full font-medium">
                Real-time from Google Flights
              </span>
            )}
            {source === 'SKYWINGS_DB' && (
              <span className="ml-auto bg-blue-100 text-blue-700 text-xs px-3 py-1 rounded-full font-medium">
                SkyWings Routes
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
            {flights.map((f, i) => <FlightCard key={i} flight={f} source={f.source || source} />)}
          </div>
        ) : (
          <div className="text-center py-16">
            <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 text-lg">No flights found for this route and date.</p>
            <p className="text-sm text-gray-400 mt-2">Try different dates or airports.</p>
          </div>
        )}
      </div>
    </div>
  );
}
