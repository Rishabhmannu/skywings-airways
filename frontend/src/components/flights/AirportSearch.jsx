import { useState, useEffect, useRef } from 'react';
import api from '../../api/axios';

export default function AirportSearch({ value, onChange, placeholder, icon: Icon, excludeCode }) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);
  const [selectedLabel, setSelectedLabel] = useState('');
  const ref = useRef();
  const inputRef = useRef();

  useEffect(() => {
    if (!value) {
      setSelectedLabel('');
      return;
    }
    // Resolve value to a label (handles swap and initial load)
    api.get(`/airports/search?q=${value}&limit=1`).then(r => {
      if (r.data.length > 0) {
        const a = r.data[0];
        setSelectedLabel(`${a.city || a.name} (${a.code})`);
      }
    }).catch(() => {});
  }, [value]);

  useEffect(() => {
    if (query.length < 2) { setResults([]); return; }
    const timer = setTimeout(() => {
      api.get(`/airports/search?q=${encodeURIComponent(query)}&limit=12`)
        .then(r => setResults(r.data.filter(a => a.code !== excludeCode)))
        .catch(() => setResults([]));
    }, 200);
    return () => clearTimeout(timer);
  }, [query, excludeCode]);

  useEffect(() => {
    const handleClick = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleSelect = (airport) => {
    onChange(airport.code);
    setSelectedLabel(`${airport.city || airport.name} (${airport.code})`);
    setQuery('');
    setOpen(false);
  };

  const handleClear = () => {
    onChange('');
    setSelectedLabel('');
    setQuery('');
    inputRef.current?.focus();
  };

  return (
    <div ref={ref} className="relative">
      <div className="relative">
        {Icon && <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />}
        <input
          ref={inputRef}
          type="text"
          value={open ? query : selectedLabel || query}
          onChange={e => { setQuery(e.target.value); setOpen(true); setSelectedLabel(''); onChange(''); }}
          onFocus={() => { if (selectedLabel) { setQuery(''); } setOpen(true); }}
          placeholder={placeholder}
          className="w-full pl-10 pr-8 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:outline-none bg-white text-gray-800 text-sm"
        />
        {(selectedLabel || query) && (
          <button type="button" onClick={handleClear}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 bg-transparent border-none cursor-pointer text-sm">
            ✕
          </button>
        )}
      </div>

      {open && results.length > 0 && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-60 overflow-y-auto">
          {results.map((a, i) => (
            <button key={i} type="button" onClick={() => handleSelect(a)}
              className="w-full px-4 py-2.5 text-left hover:bg-blue-50 transition flex items-center justify-between cursor-pointer border-none bg-transparent text-sm">
              <div>
                <span className="font-medium text-gray-800">{a.city || a.name}</span>
                {a.city && a.name && a.city !== a.name && (
                  <span className="text-gray-400 text-xs ml-1">— {a.name}</span>
                )}
              </div>
              <span className="font-mono font-bold text-[#1e3a5f] text-xs bg-blue-50 px-2 py-0.5 rounded">
                {a.code}
              </span>
            </button>
          ))}
        </div>
      )}

      {open && query.length >= 2 && results.length === 0 && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg px-4 py-3 text-sm text-gray-400">
          No airports found for "{query}"
        </div>
      )}
    </div>
  );
}
