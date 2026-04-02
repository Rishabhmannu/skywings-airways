import { useState } from 'react';

const COLS = ['A', 'B', 'C', 'D', 'E'];

export default function SeatMap({ seats, seatClass, maxSelectable, onSelectionChange }) {
  const [selected, setSelected] = useState([]);

  const filteredSeats = seats?.filter(s => s.seatClass === seatClass) || [];

  // Group seats by row number
  const rows = {};
  filteredSeats.forEach(s => {
    const row = s.seatNumber.replace(/[A-E]/g, '');
    if (!rows[row]) rows[row] = {};
    rows[row][s.seatNumber.slice(-1)] = s;
  });
  const rowNumbers = Object.keys(rows).sort((a, b) => Number(a) - Number(b));

  const toggleSeat = (seat) => {
    if (!seat.available) return;
    const isSelected = selected.some(s => s.id === seat.id);
    let next;
    if (isSelected) {
      next = selected.filter(s => s.id !== seat.id);
    } else {
      if (selected.length >= maxSelectable) return;
      next = [...selected, seat];
    }
    setSelected(next);
    onSelectionChange?.(next);
  };

  const getSeatColor = (seat) => {
    if (!seat) return 'bg-transparent';
    if (selected.some(s => s.id === seat.id)) return 'bg-[#1e3a5f] text-white ring-2 ring-blue-300';
    if (!seat.available) return 'bg-gray-200 text-gray-400 cursor-not-allowed';
    return 'bg-green-100 text-green-700 hover:bg-green-200 cursor-pointer';
  };

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-sm text-[#1e3a5f]">
          {seatClass} Class — Select {maxSelectable} seat{maxSelectable > 1 ? 's' : ''}
        </h3>
        <span className="text-xs text-gray-500">
          {selected.length}/{maxSelectable} selected
        </span>
      </div>

      {/* Legend */}
      <div className="flex gap-4 mb-4 text-xs">
        <div className="flex items-center gap-1.5">
          <div className="w-4 h-4 rounded bg-green-100 border border-green-300"></div> Available
        </div>
        <div className="flex items-center gap-1.5">
          <div className="w-4 h-4 rounded bg-[#1e3a5f]"></div> Selected
        </div>
        <div className="flex items-center gap-1.5">
          <div className="w-4 h-4 rounded bg-gray-200"></div> Taken
        </div>
      </div>

      {/* Column headers */}
      <div className="flex items-center justify-center gap-1 mb-2">
        <div className="w-8"></div>
        {COLS.slice(0, 2).map(c => (
          <div key={c} className="w-9 text-center text-xs font-bold text-gray-400">{c}</div>
        ))}
        <div className="w-6"></div> {/* Aisle */}
        <div className="w-9 text-center text-xs font-bold text-gray-400">C</div>
        <div className="w-6"></div> {/* Aisle */}
        {COLS.slice(3).map(c => (
          <div key={c} className="w-9 text-center text-xs font-bold text-gray-400">{c}</div>
        ))}
      </div>

      {/* Seat rows */}
      <div className="space-y-1.5">
        {rowNumbers.map(row => (
          <div key={row} className="flex items-center justify-center gap-1">
            <div className="w-8 text-right text-xs font-mono text-gray-400 pr-2">{row}</div>
            {/* Left block: A, B */}
            {['A', 'B'].map(col => {
              const seat = rows[row]?.[col];
              return (
                <button key={col} onClick={() => seat && toggleSeat(seat)} disabled={seat && !seat.available}
                  className={`w-9 h-8 rounded text-xs font-bold transition ${getSeatColor(seat)} border-none`}
                  title={seat ? `${seat.seatNumber} — ${seat.available ? 'Available' : 'Taken'}` : ''}>
                  {seat ? col : ''}
                </button>
              );
            })}
            {/* Aisle */}
            <div className="w-6 flex items-center justify-center">
              <div className="w-px h-6 bg-gray-200"></div>
            </div>
            {/* Middle: C */}
            {['C'].map(col => {
              const seat = rows[row]?.[col];
              return (
                <button key={col} onClick={() => seat && toggleSeat(seat)} disabled={seat && !seat.available}
                  className={`w-9 h-8 rounded text-xs font-bold transition ${getSeatColor(seat)} border-none`}
                  title={seat ? `${seat.seatNumber}` : ''}>
                  {seat ? col : ''}
                </button>
              );
            })}
            {/* Aisle */}
            <div className="w-6 flex items-center justify-center">
              <div className="w-px h-6 bg-gray-200"></div>
            </div>
            {/* Right block: D, E */}
            {['D', 'E'].map(col => {
              const seat = rows[row]?.[col];
              return (
                <button key={col} onClick={() => seat && toggleSeat(seat)} disabled={seat && !seat.available}
                  className={`w-9 h-8 rounded text-xs font-bold transition ${getSeatColor(seat)} border-none`}
                  title={seat ? `${seat.seatNumber}` : ''}>
                  {seat ? col : ''}
                </button>
              );
            })}
          </div>
        ))}
      </div>

      {/* Selected seats display */}
      {selected.length > 0 && (
        <div className="mt-4 pt-3 border-t border-gray-100">
          <p className="text-xs text-gray-500">Selected seats: <span className="font-semibold text-[#1e3a5f]">{selected.map(s => s.seatNumber).join(', ')}</span></p>
        </div>
      )}
    </div>
  );
}
