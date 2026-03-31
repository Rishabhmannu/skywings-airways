import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { Loader2, Plane, Users, CreditCard } from 'lucide-react';

export default function BookingPage() {
  const { flightId } = useParams();
  const navigate = useNavigate();
  const [flight, setFlight] = useState(null);
  const [seats, setSeats] = useState(null);
  const [seatClass, setSeatClass] = useState('ECONOMY');
  const [passengers, setPassengers] = useState([{ name: '', age: '', passportNumber: '' }]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [step, setStep] = useState(1);

  useEffect(() => {
    Promise.all([
      api.get(`/flights/${flightId}`),
      api.get(`/flights/${flightId}/seats`),
    ]).then(([fRes, sRes]) => {
      setFlight(fRes.data);
      setSeats(sRes.data);
    }).catch(() => toast.error('Failed to load flight'))
      .finally(() => setLoading(false));
  }, [flightId]);

  const addPassenger = () => {
    if (passengers.length < 6) setPassengers([...passengers, { name: '', age: '', passportNumber: '' }]);
  };

  const removePassenger = (i) => {
    if (passengers.length > 1) setPassengers(passengers.filter((_, idx) => idx !== i));
  };

  const updatePassenger = (i, field, value) => {
    const updated = [...passengers];
    updated[i] = { ...updated[i], [field]: value };
    setPassengers(updated);
  };

  const availableSeats = seats?.seats.filter(s => s.available && s.seatClass === seatClass).length || 0;

  const handleBook = async () => {
    if (passengers.some(p => !p.name || !p.age)) { toast.error('Fill all passenger details'); return; }
    if (flight.flightType === 'INTERNATIONAL' && passengers.some(p => !p.passportNumber)) {
      toast.error('Passport required for international flights'); return;
    }
    if (passengers.length > availableSeats) { toast.error('Not enough seats available'); return; }

    setSubmitting(true);
    try {
      const { data } = await api.post('/bookings', {
        flightId: Number(flightId),
        seatClass,
        passengers: passengers.map(p => ({ name: p.name, age: Number(p.age), passportNumber: p.passportNumber || null })),
      });
      toast.success('Booking created! Proceed to payment.');
      navigate(`/payment/${data.id}`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Booking failed');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;
  if (!flight) return <div className="text-center py-20 text-gray-500">Flight not found</div>;

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="flex items-center gap-2 mb-6">
        {[1, 2, 3].map(s => (
          <div key={s} className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${step >= s ? 'bg-[#1e3a5f] text-white' : 'bg-gray-200 text-gray-500'}`}>{s}</div>
            {s < 3 && <div className={`w-16 h-1 ${step > s ? 'bg-[#1e3a5f]' : 'bg-gray-200'}`} />}
          </div>
        ))}
        <span className="ml-2 text-sm text-gray-500">
          {step === 1 ? 'Select Class' : step === 2 ? 'Passengers' : 'Review'}
        </span>
      </div>

      {/* Flight Summary */}
      <div className="bg-white rounded-xl shadow-md p-6 mb-6 border border-gray-100">
        <div className="flex items-center gap-2 mb-3">
          <Plane className="w-5 h-5 text-[#1e3a5f]" />
          <span className="font-bold text-[#1e3a5f]">{flight.flightNumber}</span>
          <span className="text-gray-400 text-sm">{flight.airline}</span>
        </div>
        <p className="text-lg">{flight.origin} ({flight.originCode}) &rarr; {flight.destination} ({flight.destCode})</p>
        <p className="text-sm text-gray-500">{new Date(flight.departureTime).toLocaleString()} &bull; {flight.duration} &bull; {flight.flightType}</p>
      </div>

      {/* Step 1: Class Selection */}
      {step === 1 && (
        <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
          <h2 className="text-lg font-bold mb-4">Select Cabin Class</h2>
          <div className="grid grid-cols-2 gap-4">
            {['ECONOMY', 'BUSINESS'].map(cls => {
              const avail = seats?.seats.filter(s => s.available && s.seatClass === cls).length || 0;
              const price = cls === 'ECONOMY' ? flight.basePriceEconomy : flight.basePriceBusiness;
              return (
                <button key={cls} onClick={() => { setSeatClass(cls); setStep(2); }}
                  className={`p-6 rounded-xl border-2 text-left cursor-pointer transition ${seatClass === cls ? 'border-[#1e3a5f] bg-blue-50' : 'border-gray-200 hover:border-gray-300 bg-white'}`}>
                  <p className="font-bold text-lg">{cls}</p>
                  <p className="text-2xl font-bold text-[#1e3a5f] mt-2">{'\u20B9'}{Number(price).toLocaleString('en-IN')}</p>
                  <p className="text-sm text-gray-500 mt-1">{avail} seats available</p>
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Step 2: Passengers */}
      {step === 2 && (
        <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-bold flex items-center gap-2"><Users className="w-5 h-5" /> Passenger Details</h2>
            <button onClick={addPassenger} disabled={passengers.length >= 6}
              className="text-sm text-[#1e3a5f] font-semibold hover:underline bg-transparent border-none cursor-pointer disabled:opacity-50">
              + Add Passenger
            </button>
          </div>
          {passengers.map((p, i) => (
            <div key={i} className="border border-gray-200 rounded-lg p-4 mb-3">
              <div className="flex justify-between items-center mb-3">
                <span className="font-medium text-sm">Passenger {i + 1}</span>
                {passengers.length > 1 && (
                  <button onClick={() => removePassenger(i)} className="text-red-500 text-xs hover:underline bg-transparent border-none cursor-pointer">Remove</button>
                )}
              </div>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                <input placeholder="Full Name" value={p.name} onChange={e => updatePassenger(i, 'name', e.target.value)} required
                  className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm" />
                <input placeholder="Age" type="number" min="1" max="120" value={p.age} onChange={e => updatePassenger(i, 'age', e.target.value)} required
                  className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm" />
                {flight.flightType === 'INTERNATIONAL' && (
                  <input placeholder="Passport Number" value={p.passportNumber} onChange={e => updatePassenger(i, 'passportNumber', e.target.value)} required
                    className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm" />
                )}
              </div>
            </div>
          ))}
          <div className="flex justify-between mt-4">
            <button onClick={() => setStep(1)} className="px-4 py-2 border border-gray-300 rounded-lg text-sm cursor-pointer bg-white hover:bg-gray-50">Back</button>
            <button onClick={() => setStep(3)} className="px-6 py-2 bg-[#1e3a5f] text-white rounded-lg text-sm font-semibold cursor-pointer border-none hover:bg-[#2a4d7a]">Review</button>
          </div>
        </div>
      )}

      {/* Step 3: Review */}
      {step === 3 && (
        <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
          <h2 className="text-lg font-bold mb-4 flex items-center gap-2"><CreditCard className="w-5 h-5" /> Review & Book</h2>
          <div className="space-y-2 mb-4 text-sm">
            <p><strong>Class:</strong> {seatClass}</p>
            <p><strong>Passengers:</strong> {passengers.length}</p>
            {passengers.map((p, i) => (
              <p key={i} className="text-gray-600 ml-4">{i + 1}. {p.name}, Age {p.age} {p.passportNumber && `| Passport: ${p.passportNumber}`}</p>
            ))}
          </div>
          <div className="bg-blue-50 rounded-lg p-4 mb-4">
            <p className="text-xs text-gray-500">Estimated price (final price calculated on server)</p>
            <p className="text-xl font-bold text-[#1e3a5f]">
              {'\u20B9'}{Number(seatClass === 'ECONOMY' ? flight.basePriceEconomy : flight.basePriceBusiness).toLocaleString('en-IN')} x {passengers.length} pax
            </p>
          </div>
          <div className="flex justify-between">
            <button onClick={() => setStep(2)} className="px-4 py-2 border border-gray-300 rounded-lg text-sm cursor-pointer bg-white hover:bg-gray-50">Back</button>
            <button onClick={handleBook} disabled={submitting}
              className="px-8 py-3 bg-[#1e3a5f] text-white rounded-lg font-semibold cursor-pointer border-none hover:bg-[#2a4d7a] disabled:opacity-50">
              {submitting ? 'Booking...' : 'Confirm & Proceed to Payment'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
