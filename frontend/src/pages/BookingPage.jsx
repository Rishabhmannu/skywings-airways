import { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { Loader2, Plane, Users, CreditCard } from 'lucide-react';

const emptyPassenger = { name: '', age: '', passportNumber: '', gender: '', dateOfBirth: '', nationality: '', mealPreference: 'NO_PREFERENCE', specialAssistance: 'NONE' };

const FARE_TYPES = [
  { value: 'REGULAR', label: 'Regular', discount: null },
  { value: 'STUDENT', label: 'Student', discount: '10% off' },
  { value: 'ARMED_FORCES', label: 'Armed Forces', discount: '15% off' },
  { value: 'SENIOR_CITIZEN', label: 'Senior Citizen', discount: '12% off' },
  { value: 'MEDICAL', label: 'Doctors & Nurses', discount: '10% off' },
];

export default function BookingPage() {
  const { flightId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [flight, setFlight] = useState(null);
  const [seats, setSeats] = useState(null);
  const [seatClass, setSeatClass] = useState('ECONOMY');
  const [fareType, setFareType] = useState(searchParams.get('fareType') || 'REGULAR');
  const [passengers, setPassengers] = useState([{ ...emptyPassenger }]);
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
    if (passengers.length < 6) setPassengers([...passengers, { ...emptyPassenger }]);
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
    if (passengers.some(p => !p.name || !p.age)) { toast.error('Fill name and age for all passengers'); return; }
    if (flight.flightType === 'INTERNATIONAL' && passengers.some(p => !p.passportNumber)) {
      toast.error('Passport required for international flights'); return;
    }
    if (passengers.length > availableSeats) { toast.error('Not enough seats available'); return; }

    setSubmitting(true);
    try {
      const { data } = await api.post('/bookings', {
        flightId: Number(flightId),
        seatClass,
        fareType,
        passengers: passengers.map(p => ({
          name: p.name,
          age: Number(p.age),
          passportNumber: p.passportNumber || null,
          gender: p.gender || null,
          dateOfBirth: p.dateOfBirth || null,
          nationality: p.nationality || null,
          mealPreference: p.mealPreference !== 'NO_PREFERENCE' ? p.mealPreference : null,
          specialAssistance: p.specialAssistance !== 'NONE' ? p.specialAssistance : null,
        })),
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

  const selectClass = "px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm bg-white appearance-auto";
  const inputClass = "px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none text-sm";

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      {/* Stepper */}
      <div className="flex items-center gap-2 mb-6">
        {[1, 2, 3].map(s => (
          <div key={s} className="flex items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${step >= s ? 'bg-[#1e3a5f] text-white' : 'bg-gray-200 text-gray-500'}`}>{s}</div>
            {s < 3 && <div className={`w-16 h-1 ${step > s ? 'bg-[#1e3a5f]' : 'bg-gray-200'}`} />}
          </div>
        ))}
        <span className="ml-2 text-sm text-gray-500">
          {step === 1 ? 'Class & Fare' : step === 2 ? 'Passengers' : 'Review'}
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

      {/* Step 1: Class + Fare Selection */}
      {step === 1 && (
        <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
          <h2 className="text-lg font-bold mb-4">Select Cabin Class</h2>
          <div className="grid grid-cols-2 gap-4 mb-6">
            {['ECONOMY', 'BUSINESS'].map(cls => {
              const avail = seats?.seats.filter(s => s.available && s.seatClass === cls).length || 0;
              const price = cls === 'ECONOMY' ? flight.basePriceEconomy : flight.basePriceBusiness;
              return (
                <button key={cls} onClick={() => setSeatClass(cls)}
                  className={`p-5 rounded-xl border-2 text-left cursor-pointer transition ${seatClass === cls ? 'border-[#1e3a5f] bg-blue-50' : 'border-gray-200 hover:border-gray-300 bg-white'}`}>
                  <p className="font-bold text-lg">{cls}</p>
                  <p className="text-2xl font-bold text-[#1e3a5f] mt-2">{'\u20B9'}{Number(price).toLocaleString('en-IN')}</p>
                  <p className="text-sm text-gray-500 mt-1">{avail} seats available</p>
                </button>
              );
            })}
          </div>

          <h2 className="text-lg font-bold mb-3">Special Fare</h2>
          <div className="flex flex-wrap gap-2 mb-6">
            {FARE_TYPES.map(f => (
              <button key={f.value} onClick={() => setFareType(f.value)}
                className={`px-4 py-2 rounded-lg text-sm font-medium cursor-pointer border transition ${
                  fareType === f.value ? 'bg-[#1e3a5f] text-white border-[#1e3a5f]' : 'bg-white text-gray-600 border-gray-200 hover:border-gray-400'
                }`}>
                {f.label}
                {f.discount && <span className="ml-1 opacity-70">({f.discount})</span>}
              </button>
            ))}
          </div>

          <button onClick={() => setStep(2)}
            className="w-full bg-[#1e3a5f] text-white py-3 rounded-lg hover:bg-[#2a4d7a] transition font-semibold cursor-pointer border-none">
            Continue to Passenger Details
          </button>
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
            <div key={i} className="border border-gray-200 rounded-lg p-4 mb-4">
              <div className="flex justify-between items-center mb-3">
                <span className="font-medium text-sm">Passenger {i + 1}</span>
                {passengers.length > 1 && (
                  <button onClick={() => removePassenger(i)} className="text-red-500 text-xs hover:underline bg-transparent border-none cursor-pointer">Remove</button>
                )}
              </div>

              {/* Row 1: Name, Age, Gender */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                <input placeholder="Full Name *" value={p.name} onChange={e => updatePassenger(i, 'name', e.target.value)} required className={inputClass} />
                <input placeholder="Age *" type="number" min="1" max="120" value={p.age} onChange={e => updatePassenger(i, 'age', e.target.value)} required className={inputClass} />
                <select value={p.gender} onChange={e => updatePassenger(i, 'gender', e.target.value)} className={selectClass}>
                  <option value="">Gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              {/* Row 2: DOB, Nationality, Passport */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-3">
                <div>
                  <label className="text-xs text-gray-400">Date of Birth</label>
                  <input type="date" value={p.dateOfBirth} onChange={e => updatePassenger(i, 'dateOfBirth', e.target.value)} className={inputClass + ' w-full'} />
                </div>
                <input placeholder="Nationality" value={p.nationality} onChange={e => updatePassenger(i, 'nationality', e.target.value)} className={inputClass} />
                {flight.flightType === 'INTERNATIONAL' && (
                  <input placeholder="Passport Number *" value={p.passportNumber} onChange={e => updatePassenger(i, 'passportNumber', e.target.value)} required className={inputClass} />
                )}
                {flight.flightType !== 'INTERNATIONAL' && (
                  <input placeholder="Passport Number" value={p.passportNumber} onChange={e => updatePassenger(i, 'passportNumber', e.target.value)} className={inputClass} />
                )}
              </div>

              {/* Row 3: Meal, Special Assistance */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <select value={p.mealPreference} onChange={e => updatePassenger(i, 'mealPreference', e.target.value)} className={selectClass}>
                  <option value="NO_PREFERENCE">Meal: No Preference</option>
                  <option value="VEG">Vegetarian</option>
                  <option value="NON_VEG">Non-Vegetarian</option>
                  <option value="VEGAN">Vegan</option>
                  <option value="JAIN">Jain</option>
                </select>
                <select value={p.specialAssistance} onChange={e => updatePassenger(i, 'specialAssistance', e.target.value)} className={selectClass}>
                  <option value="NONE">Special Assistance: None</option>
                  <option value="WHEELCHAIR">Wheelchair Required</option>
                  <option value="VISUAL_IMPAIRMENT">Visual Impairment</option>
                  <option value="HEARING_IMPAIRMENT">Hearing Impairment</option>
                  <option value="ELDERLY_ASSISTANCE">Elderly Assistance</option>
                  <option value="UNACCOMPANIED_MINOR">Unaccompanied Minor</option>
                </select>
              </div>

              {/* Auto-detect senior citizen */}
              {Number(p.age) >= 60 && (
                <p className="text-xs text-amber-600 mt-2 bg-amber-50 px-3 py-1 rounded-lg inline-block">
                  Senior Citizen — eligible for additional benefits
                </p>
              )}
            </div>
          ))}
          <div className="flex justify-between mt-4">
            <button onClick={() => setStep(1)} className="px-4 py-2 border border-gray-300 rounded-lg text-sm cursor-pointer bg-white hover:bg-gray-50">Back</button>
            <button onClick={() => setStep(3)} className="px-6 py-2 bg-[#1e3a5f] text-white rounded-lg text-sm font-semibold cursor-pointer border-none hover:bg-[#2a4d7a]">Review Booking</button>
          </div>
        </div>
      )}

      {/* Step 3: Review */}
      {step === 3 && (
        <div className="bg-white rounded-xl shadow-md p-6 border border-gray-100">
          <h2 className="text-lg font-bold mb-4 flex items-center gap-2"><CreditCard className="w-5 h-5" /> Review & Book</h2>
          <div className="space-y-2 mb-4 text-sm">
            <p><strong>Class:</strong> {seatClass}</p>
            <p><strong>Fare:</strong> {FARE_TYPES.find(f => f.value === fareType)?.label} {FARE_TYPES.find(f => f.value === fareType)?.discount && `(${FARE_TYPES.find(f => f.value === fareType)?.discount})`}</p>
            <p><strong>Passengers:</strong> {passengers.length}</p>
            {passengers.map((p, i) => (
              <div key={i} className="ml-4 text-gray-600 border-l-2 border-gray-200 pl-3 py-1">
                <p className="font-medium">{i + 1}. {p.name}, {p.gender || ''} Age {p.age}</p>
                <p className="text-xs text-gray-400">
                  {[
                    p.nationality,
                    p.passportNumber && `Passport: ${p.passportNumber}`,
                    p.mealPreference !== 'NO_PREFERENCE' && `Meal: ${p.mealPreference}`,
                    p.specialAssistance !== 'NONE' && p.specialAssistance?.replace('_', ' '),
                    Number(p.age) >= 60 && 'Senior Citizen',
                  ].filter(Boolean).join(' | ') || 'No additional details'}
                </p>
              </div>
            ))}
          </div>
          <div className="bg-blue-50 rounded-lg p-4 mb-4">
            <p className="text-xs text-gray-500">Estimated price (final calculated on server with fare discount)</p>
            <p className="text-xl font-bold text-[#1e3a5f]">
              {'\u20B9'}{Number(seatClass === 'ECONOMY' ? flight.basePriceEconomy : flight.basePriceBusiness).toLocaleString('en-IN')} x {passengers.length} pax
              {fareType !== 'REGULAR' && <span className="text-sm font-normal text-green-600 ml-2">({FARE_TYPES.find(f => f.value === fareType)?.discount} applied)</span>}
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
