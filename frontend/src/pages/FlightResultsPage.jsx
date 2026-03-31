import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../api/axios';
import FlightCard from '../components/flights/FlightCard';
import FlightSearchForm from '../components/flights/FlightSearchForm';
import { Loader2, AlertCircle } from 'lucide-react';

export default function FlightResultsPage() {
  const [searchParams] = useSearchParams();
  const [dbFlights, setDbFlights] = useState([]);
  const [liveFlights, setLiveFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('skywings');

  const origin = searchParams.get('origin');
  const dest = searchParams.get('dest');
  const date = searchParams.get('date');

  useEffect(() => {
    if (!origin || !dest || !date) return;
    setLoading(true);

    const fetchDB = api.get(`/flights/search?origin=${origin}&dest=${dest}&date=${date}`)
      .then(r => setDbFlights(r.data)).catch(() => setDbFlights([]));

    const fetchLive = api.get(`/flights/live-search?origin=${origin}&dest=${dest}&date=${date}&adults=1`)
      .then(r => setLiveFlights(r.data)).catch(() => setLiveFlights([]));

    Promise.allSettled([fetchDB, fetchLive]).finally(() => setLoading(false));
  }, [origin, dest, date]);

  const hasLive = liveFlights.length > 0 && liveFlights.some(f => f.source === 'AMADEUS');

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <FlightSearchForm />

      <div className="mt-8">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" />
            <span className="ml-3 text-gray-500">Searching flights...</span>
          </div>
        ) : (
          <>
            {hasLive && (
              <div className="flex gap-2 mb-6">
                <button onClick={() => setTab('skywings')}
                  className={`px-4 py-2 rounded-lg text-sm font-medium cursor-pointer border-none transition ${tab === 'skywings' ? 'bg-[#1e3a5f] text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                  SkyWings Flights ({dbFlights.length})
                </button>
                <button onClick={() => setTab('live')}
                  className={`px-4 py-2 rounded-lg text-sm font-medium cursor-pointer border-none transition ${tab === 'live' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                  Live Flights ({liveFlights.length})
                </button>
              </div>
            )}

            {tab === 'skywings' && (
              dbFlights.length > 0 ? (
                <div className="space-y-4">
                  {dbFlights.map(f => <FlightCard key={f.id} flight={f} source="SKYWINGS_DB" />)}
                </div>
              ) : (
                <div className="text-center py-16">
                  <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                  <p className="text-gray-500">No SkyWings flights found for this route and date.</p>
                  {hasLive && <p className="text-sm text-gray-400 mt-2">Check the Live Flights tab for real-time results.</p>}
                </div>
              )
            )}

            {tab === 'live' && (
              <div className="space-y-4">
                {liveFlights.map((f, i) => <FlightCard key={i} flight={f} source={f.source} />)}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
