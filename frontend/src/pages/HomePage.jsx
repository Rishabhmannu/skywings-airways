import FlightSearchForm from '../components/flights/FlightSearchForm';
import { Plane, Shield, CreditCard, Mail } from 'lucide-react';

export default function HomePage() {
  return (
    <div>
      <section className="bg-gradient-to-br from-[#1e3a5f] to-[#2a4d7a] text-white py-20 px-4">
        <div className="max-w-4xl mx-auto text-center mb-10">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">Fly with SkyWings Airways</h1>
          <p className="text-blue-200 text-lg">Search real flights, book with simulated payments, and get your e-ticket instantly.</p>
        </div>
        <FlightSearchForm />
      </section>

      <section className="max-w-6xl mx-auto py-16 px-4">
        <h2 className="text-2xl font-bold text-center mb-10 text-[#1e3a5f]">How It Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {[
            { icon: <Plane className="w-8 h-8" />, title: 'Search Flights', desc: 'Real-time data from Amadeus API with live pricing' },
            { icon: <Shield className="w-8 h-8" />, title: 'Select & Book', desc: 'Choose seats, enter passenger details, review your booking' },
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
    </div>
  );
}
