import { useState, useEffect } from 'react';
import api from '../../api/axios';
import toast from 'react-hot-toast';
import { Loader2, Plus, Trash2, Plane } from 'lucide-react';

export default function ManageFlightsPage() {
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    flightNumber: '', origin: '', originCode: '', destination: '', destCode: '',
    departureTime: '', arrivalTime: '', flightType: 'DOMESTIC',
    basePriceEconomy: '', basePriceBusiness: '',
  });

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
      setShowForm(false);
      setForm({ flightNumber: '', origin: '', originCode: '', destination: '', destCode: '', departureTime: '', arrivalTime: '', flightType: 'DOMESTIC', basePriceEconomy: '', basePriceBusiness: '' });
      fetchFlights();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create flight');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this flight?')) return;
    try {
      await api.delete(`/flights/${id}`);
      toast.success('Flight deleted');
      fetchFlights();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#1e3a5f] flex items-center gap-2"><Plane className="w-6 h-6" /> Manage Flights</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-1 bg-[#1e3a5f] text-white px-4 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-[#2a4d7a] text-sm">
          <Plus className="w-4 h-4" /> Add Flight
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-white rounded-xl shadow-md p-6 mb-6 border border-gray-100">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              ['flightNumber', 'Flight No (SW-XXX)'], ['origin', 'Origin City'], ['originCode', 'Origin Code'],
              ['destination', 'Dest City'], ['destCode', 'Dest Code'], ['flightType', ''],
              ['basePriceEconomy', 'Economy Price'], ['basePriceBusiness', 'Business Price'],
            ].map(([key, placeholder]) => (
              key === 'flightType' ? (
                <select key={key} value={form.flightType} onChange={e => setForm({ ...form, flightType: e.target.value })}
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm">
                  <option value="DOMESTIC">Domestic</option>
                  <option value="INTERNATIONAL">International</option>
                </select>
              ) : (
                <input key={key} placeholder={placeholder} value={form[key]}
                  onChange={e => setForm({ ...form, [key]: e.target.value })} required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none" />
              )
            ))}
            <input type="datetime-local" placeholder="Departure" value={form.departureTime}
              onChange={e => setForm({ ...form, departureTime: e.target.value })} required
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            <input type="datetime-local" placeholder="Arrival" value={form.arrivalTime}
              onChange={e => setForm({ ...form, arrivalTime: e.target.value })} required
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
          </div>
          <button type="submit" className="mt-4 bg-green-600 text-white px-6 py-2 rounded-lg font-semibold cursor-pointer border-none hover:bg-green-700 text-sm">Create Flight</button>
        </form>
      )}

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
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {flights.map(f => (
              <tr key={f.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-bold text-[#1e3a5f]">{f.flightNumber}</td>
                <td className="px-4 py-3">{f.originCode} &rarr; {f.destCode}</td>
                <td className="px-4 py-3 text-xs">{new Date(f.departureTime).toLocaleString()}</td>
                <td className="px-4 py-3"><span className={`text-xs px-2 py-1 rounded-full ${f.flightType === 'INTERNATIONAL' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'}`}>{f.flightType}</span></td>
                <td className="px-4 py-3 text-right">{'\u20B9'}{Number(f.basePriceEconomy).toLocaleString('en-IN')}</td>
                <td className="px-4 py-3 text-right">{'\u20B9'}{Number(f.basePriceBusiness).toLocaleString('en-IN')}</td>
                <td className="px-4 py-3 text-xs">E:{f.availableEconomySeats} B:{f.availableBusinessSeats}</td>
                <td className="px-4 py-3">
                  <button onClick={() => handleDelete(f.id)} className="text-red-500 hover:text-red-700 bg-transparent border-none cursor-pointer"><Trash2 className="w-4 h-4" /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
