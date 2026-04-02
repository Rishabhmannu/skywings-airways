import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import FlightSearchForm from '../components/flights/FlightSearchForm';
import { Plane, Shield, CreditCard, Mail, ArrowRight, Globe, TrendingUp } from 'lucide-react';

const popularRoutes = [
  { from: 'DEL', to: 'BOM', fromCity: 'New Delhi', toCity: 'Mumbai', tag: 'Most Popular' },
  { from: 'BLR', to: 'DEL', fromCity: 'Bangalore', toCity: 'New Delhi', tag: 'Business' },
  { from: 'DEL', to: 'DXB', fromCity: 'New Delhi', toCity: 'Dubai', tag: 'International' },
  { from: 'BOM', to: 'SIN', fromCity: 'Mumbai', toCity: 'Singapore', tag: 'International' },
  { from: 'CCU', to: 'BOM', fromCity: 'Kolkata', toCity: 'Mumbai', tag: 'Domestic' },
  { from: 'DEL', to: 'LHR', fromCity: 'New Delhi', toCity: 'London', tag: 'Long Haul' },
];

export default function HomePage() {
  const navigate = useNavigate();
  const [dbFlights, setDbFlights] = useState([]);
  const [flightsError, setFlightsError] = useState(false);

  useEffect(() => {
    api.get('/flights/search?origin=DEL&dest=BOM&date=' + getNextWeekDate())
      .then(r => setDbFlights(r.data.slice(0, 3)))
      .catch(() => setFlightsError(true));
  }, []);

  function getNextWeekDate() {
    const d = new Date();
    d.setDate(d.getDate() + 8);
    return d.toISOString().split('T')[0];
  }

  const searchRoute = (from, to) => {
    const d = new Date();
    d.setDate(d.getDate() + 7);
    const date = d.toISOString().split('T')[0];
    navigate(`/flights?origin=${from}&dest=${to}&date=${date}`);
  };

  return (
    <div>
      {/* Hero */}
      <section className="bg-gradient-to-br from-[#1e3a5f] via-[#2a4d7a] to-[#1a3352] text-white py-20 px-4">
        <div className="max-w-4xl mx-auto text-center mb-10">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Fly with SkyWings Airways</h1>
          <p className="text-blue-200 text-lg">Search real flights from Google Flights, book with simulated payments, and get your e-ticket instantly.</p>
        </div>
        <FlightSearchForm />
      </section>

      {/* Popular Routes */}
      <section className="max-w-6xl mx-auto py-16 px-4">
        <div className="flex items-center gap-3 mb-8">
          <TrendingUp className="w-6 h-6 text-[#1e3a5f]" />
          <h2 className="text-2xl font-bold text-[#1e3a5f]">Popular Routes</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {popularRoutes.map((route, i) => (
            <button key={i} onClick={() => searchRoute(route.from, route.to)}
              className="group bg-white rounded-xl p-5 border border-gray-100 shadow-sm hover:shadow-md hover:border-blue-200 transition text-left cursor-pointer w-full">
              <div className="flex items-center justify-between mb-3">
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                  route.tag === 'International' ? 'bg-purple-100 text-purple-700' :
                  route.tag === 'Most Popular' ? 'bg-amber-100 text-amber-700' :
                  route.tag === 'Long Haul' ? 'bg-red-100 text-red-700' :
                  route.tag === 'Business' ? 'bg-blue-100 text-blue-700' :
                  'bg-green-100 text-green-700'
                }`}>{route.tag}</span>
                <ArrowRight className="w-4 h-4 text-gray-300 group-hover:text-[#1e3a5f] transition" />
              </div>
              <div className="flex items-center gap-3">
                <div>
                  <p className="font-bold text-gray-800">{route.fromCity}</p>
                  <p className="text-xs text-gray-400">{route.from}</p>
                </div>
                <Plane className="w-4 h-4 text-[#1e3a5f] mx-2" />
                <div>
                  <p className="font-bold text-gray-800">{route.toCity}</p>
                  <p className="text-xs text-gray-400">{route.to}</p>
                </div>
              </div>
            </button>
          ))}
        </div>
      </section>

      {/* How It Works */}
      <section className="max-w-6xl mx-auto py-16 px-4 border-t border-gray-100">
        <h2 className="text-2xl font-bold text-center mb-10 text-[#1e3a5f]">How It Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {[
            { icon: <Globe className="w-8 h-8" />, title: 'Search Flights', desc: 'Real-time data from Google Flights with live pricing from actual airlines' },
            { icon: <Plane className="w-8 h-8" />, title: 'Select & Book', desc: 'Choose seats, enter passenger details, review your booking' },
            { icon: <CreditCard className="w-8 h-8" />, title: 'Simulated Payment', desc: 'Card validation + OTP via SMS & Email. No real charges.' },
            { icon: <Mail className="w-8 h-8" />, title: 'Get E-Ticket', desc: 'PDF ticket with QR code delivered to your email' },
          ].map((item, i) => (
            <div key={i} className="text-center p-6 rounded-xl bg-white shadow-sm border border-gray-100">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-50 text-[#1e3a5f] rounded-full mb-4">
                {item.icon}
              </div>
              <h3 className="font-semibold mb-2">{item.title}</h3>
              <p className="text-sm text-gray-500">{item.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* SkyWings Flights Preview */}
      {flightsError && (
        <section className="max-w-6xl mx-auto py-8 px-4 border-t border-gray-100">
          <p className="text-center text-sm text-gray-400">Could not load SkyWings flights. The backend may be starting up.</p>
        </section>
      )}
      {dbFlights.length > 0 && (
        <section className="max-w-6xl mx-auto py-16 px-4 border-t border-gray-100">
          <div className="flex items-center gap-3 mb-8">
            <Plane className="w-6 h-6 text-[#1e3a5f]" />
            <h2 className="text-2xl font-bold text-[#1e3a5f]">SkyWings Direct Flights</h2>
            <span className="text-sm text-gray-400 ml-2">Bookable with full experience</span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {dbFlights.map(f => (
              <div key={f.id} className="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
                <div className="flex items-center gap-2 mb-3">
                  <span className="font-bold text-[#1e3a5f]">{f.flightNumber}</span>
                  <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">{f.flightType}</span>
                </div>
                <p className="text-sm font-medium">{f.origin} &rarr; {f.destination}</p>
                <p className="text-xs text-gray-400 mt-1">{f.duration} &bull; {f.availableEconomySeats} economy seats</p>
                <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
                  <p className="text-lg font-bold text-[#1e3a5f]">{'\u20B9'}{Number(f.basePriceEconomy).toLocaleString('en-IN')}</p>
                  <button onClick={() => navigate(`/booking/${f.id}`)}
                    className="text-sm bg-[#1e3a5f] text-white px-4 py-1.5 rounded-lg hover:bg-[#2a4d7a] transition cursor-pointer border-none font-medium">
                    Book
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
