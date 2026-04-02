import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Loader2, Plus, Trash2, Plane, Pencil, X } from 'lucide-react';

const emptyForm = {
  flightNumber: '', origin: '', originCode: '', destination: '', destCode: '',
  departureTime: '', arrivalTime: '', flightType: 'DOMESTIC',
  basePriceEconomy: '', basePriceBusiness: '',
};

export default function ManageFlightsPage() {
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [editingFlight, setEditingFlight] = useState(null);
  const [form, setForm] = useState({ ...emptyForm });

  const fetchFlights = () => {
    setLoading(true);
    api.get('/admin/flights').then(r => setFlights(r.data)).finally(() => setLoading(false));
  };

  useEffect(() => { fetchFlights(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.post('/flights', {
        ...form,
        basePriceEconomy: Number(form.basePriceEconomy),
        basePriceBusiness: Number(form.basePriceBusiness),
      });
      toast.success('Flight created with 40 seats');
      setShowCreate(false);
      setForm({ ...emptyForm });
      fetchFlights();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create flight');
    }
  };

  const startEdit = (flight) => {
    setEditingFlight(flight.id);
    setShowCreate(false);
    setForm({
      origin: flight.origin || '', originCode: flight.originCode || '',
      destination: flight.destination || '', destCode: flight.destCode || '',
      departureTime: flight.departureTime ? flight.departureTime.slice(0, 16) : '',
      arrivalTime: flight.arrivalTime ? flight.arrivalTime.slice(0, 16) : '',
      flightType: flight.flightType || 'DOMESTIC',
      basePriceEconomy: flight.basePriceEconomy || '',
      basePriceBusiness: flight.basePriceBusiness || '',
    });
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/flights/${editingFlight}`, {
        origin: form.origin, originCode: form.originCode,
        destination: form.destination, destCode: form.destCode,
        departureTime: form.departureTime, arrivalTime: form.arrivalTime,
        flightType: form.flightType,
        basePriceEconomy: Number(form.basePriceEconomy),
        basePriceBusiness: Number(form.basePriceBusiness),
      });
      toast.success('Flight updated');
      setEditingFlight(null);
      setForm({ ...emptyForm });
      fetchFlights();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    }
  };

  const cancelEdit = () => { setEditingFlight(null); setForm({ ...emptyForm }); };

  const handleDelete = async (id) => {
    if (!confirm('Delete this flight? This cannot be undone.')) return;
    try {
      await api.delete(`/flights/${id}`);
      toast.success('Flight deleted');
      fetchFlights();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  const inputClass = "px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none";
  const selectClass = "px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white appearance-auto";

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#1e3a5f] flex items-center gap-2"><Plane className="w-6 h-6" /> Manage Flights</h1>
        <button onClick={() => { setShowCreate(!showCreate); setEditingFlight(null); setForm({ ...emptyForm }); }}
          className="flex items-center gap-1 bg-[#1e3a5f] text-white px-4 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-[#2a4d7a] text-sm">
          <Plus className="w-4 h-4" /> Add Flight
        </button>
      </div>

      {/* Create Form */}
      {showCreate && (
        <form onSubmit={handleCreate} className="bg-white rounded-xl shadow-md p-6 mb-6 border border-gray-100">
          <h3 className="font-semibold mb-4 text-[#1e3a5f]">New Flight</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <input placeholder="Flight Number" value={form.flightNumber} onChange={e => setForm({ ...form, flightNumber: e.target.value })} required className={inputClass} />
            <input placeholder="Origin City" value={form.origin} onChange={e => setForm({ ...form, origin: e.target.value })} required className={inputClass} />
            <input placeholder="Origin Code" value={form.originCode} onChange={e => setForm({ ...form, originCode: e.target.value })} required className={inputClass} />
            <input placeholder="Destination City" value={form.destination} onChange={e => setForm({ ...form, destination: e.target.value })} required className={inputClass} />
            <input placeholder="Dest Code" value={form.destCode} onChange={e => setForm({ ...form, destCode: e.target.value })} required className={inputClass} />
            <select value={form.flightType} onChange={e => setForm({ ...form, flightType: e.target.value })} className={selectClass}>
              <option value="DOMESTIC">Domestic</option>
              <option value="INTERNATIONAL">International</option>
            </select>
            <input placeholder="Economy Price" type="number" value={form.basePriceEconomy} onChange={e => setForm({ ...form, basePriceEconomy: e.target.value })} required className={inputClass} />
            <input placeholder="Business Price" type="number" value={form.basePriceBusiness} onChange={e => setForm({ ...form, basePriceBusiness: e.target.value })} required className={inputClass} />
            <input type="datetime-local" value={form.departureTime} onChange={e => setForm({ ...form, departureTime: e.target.value })} required className={inputClass} />
            <input type="datetime-local" value={form.arrivalTime} onChange={e => setForm({ ...form, arrivalTime: e.target.value })} required className={inputClass} />
          </div>
          <button type="submit" className="mt-4 bg-green-600 text-white px-6 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-green-700 text-sm">Create Flight</button>
        </form>
      )}

      {/* Edit Form */}
      {editingFlight && (
        <form onSubmit={handleUpdate} className="bg-amber-50 rounded-xl shadow-md p-6 mb-6 border border-amber-200">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-amber-800">Editing Flight #{editingFlight}</h3>
            <button type="button" onClick={cancelEdit} className="text-gray-400 hover:text-gray-600 bg-transparent border-none cursor-pointer"><X className="w-5 h-5" /></button>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <input placeholder="Origin City" value={form.origin} onChange={e => setForm({ ...form, origin: e.target.value })} className={inputClass} />
            <input placeholder="Origin Code" value={form.originCode} onChange={e => setForm({ ...form, originCode: e.target.value })} className={inputClass} />
            <input placeholder="Destination City" value={form.destination} onChange={e => setForm({ ...form, destination: e.target.value })} className={inputClass} />
            <input placeholder="Dest Code" value={form.destCode} onChange={e => setForm({ ...form, destCode: e.target.value })} className={inputClass} />
            <select value={form.flightType} onChange={e => setForm({ ...form, flightType: e.target.value })} className={selectClass}>
              <option value="DOMESTIC">Domestic</option>
              <option value="INTERNATIONAL">International</option>
            </select>
            <input placeholder="Economy Price" type="number" value={form.basePriceEconomy} onChange={e => setForm({ ...form, basePriceEconomy: e.target.value })} className={inputClass} />
            <input placeholder="Business Price" type="number" value={form.basePriceBusiness} onChange={e => setForm({ ...form, basePriceBusiness: e.target.value })} className={inputClass} />
            <input type="datetime-local" value={form.departureTime} onChange={e => setForm({ ...form, departureTime: e.target.value })} className={inputClass} />
            <input type="datetime-local" value={form.arrivalTime} onChange={e => setForm({ ...form, arrivalTime: e.target.value })} className={inputClass} />
          </div>
          <div className="flex gap-3 mt-4">
            <button type="submit" className="bg-amber-600 text-white px-6 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-amber-700 text-sm">Save Changes</button>
            <button type="button" onClick={cancelEdit} className="bg-gray-200 text-gray-700 px-6 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-gray-300 text-sm">Cancel</button>
          </div>
        </form>
      )}

      {/* Flights Table */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600">
            <tr>
              <th className="px-4 py-3 text-left">Flight</th>
              <th className="px-4 py-3 text-left">Route</th>
              <th className="px-4 py-3 text-left">Departure</th>
              <th className="px-4 py-3 text-left">Type</th>
              <th className="px-4 py-3 text-right">Economy</th>
              <th className="px-4 py-3 text-right">Business</th>
              <th className="px-4 py-3 text-left">Seats</th>
              <th className="px-4 py-3 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {flights.map(f => (
              <tr key={f.id} className={`hover:bg-gray-50 ${editingFlight === f.id ? 'bg-amber-50' : ''}`}>
                <td className="px-4 py-3 font-bold text-[#1e3a5f]">{f.flightNumber}</td>
                <td className="px-4 py-3">{f.originCode} &rarr; {f.destCode}</td>
                <td className="px-4 py-3 text-xs">{new Date(f.departureTime).toLocaleString()}</td>
                <td className="px-4 py-3"><span className={`text-xs px-2 py-1 rounded-full ${f.flightType === 'INTERNATIONAL' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}`}>{f.flightType}</span></td>
                <td className="px-4 py-3 text-right">{'\u20B9'}{Number(f.basePriceEconomy).toLocaleString('en-IN')}</td>
                <td className="px-4 py-3 text-right">{'\u20B9'}{Number(f.basePriceBusiness).toLocaleString('en-IN')}</td>
                <td className="px-4 py-3 text-xs">E:{f.availableEconomySeats} B:{f.availableBusinessSeats}</td>
                <td className="px-4 py-3 flex gap-2 justify-center">
                  <button onClick={() => startEdit(f)} className="text-amber-600 hover:text-amber-800 bg-transparent border-none cursor-pointer" title="Edit"><Pencil className="w-4 h-4" /></button>
                  <button onClick={() => handleDelete(f.id)} className="text-red-500 hover:text-red-700 bg-transparent border-none cursor-pointer" title="Delete"><Trash2 className="w-4 h-4" /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
