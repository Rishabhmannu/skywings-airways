import { Plane, Shield, Globe, Zap } from 'lucide-react';

export default function AboutPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-12">
      <div className="text-center mb-12">
        <Plane className="w-16 h-16 text-[#1e3a5f] mx-auto mb-4" />
        <h1 className="text-3xl font-bold text-[#1e3a5f] mb-4">About SkyWings Airways</h1>
        <p className="text-gray-500 max-w-2xl mx-auto">
          SkyWings Airways is a full-stack airline ticket booking system built as a portfolio project.
          It demonstrates modern Java and React development practices with real-world integrations.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
        {[
          { icon: <Globe className="w-6 h-6" />, title: 'Real Flight Data', desc: 'Live pricing from Amadeus API with 15+ seeded SkyWings routes across India and international destinations.' },
          { icon: <Shield className="w-6 h-6" />, title: 'Secure & Modern', desc: 'JWT authentication, BCrypt password hashing, role-based access control, and Spring Security integration.' },
          { icon: <Zap className="w-6 h-6" />, title: 'Simulated Payments', desc: 'Luhn card validation with dual-channel OTP (SMS + Email). Looks real, but no money is charged.' },
          { icon: <Plane className="w-6 h-6" />, title: 'E-Tickets', desc: 'PDF tickets with QR codes generated on the fly and delivered to your email automatically.' },
        ].map((item, i) => (
          <div key={i} className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
            <div className="inline-flex items-center justify-center w-12 h-12 bg-blue-50 text-[#1e3a5f] rounded-lg mb-3">
              {item.icon}
            </div>
            <h3 className="font-semibold mb-2">{item.title}</h3>
            <p className="text-sm text-gray-500">{item.desc}</p>
          </div>
        ))}
      </div>

      <div className="bg-[#1e3a5f] text-white rounded-2xl p-8">
        <h2 className="text-xl font-bold mb-4">Tech Stack</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          {[
            'Java 21', 'Spring Boot 3.3', 'PostgreSQL', 'Redis',
            'React 18', 'Tailwind CSS', 'JWT Auth', 'Amadeus API',
            'Twilio SMS', 'SendGrid Email', 'OpenPDF', 'Docker',
          ].map(t => (
            <div key={t} className="bg-white/10 rounded-lg px-3 py-2 text-center">{t}</div>
          ))}
        </div>
      </div>
    </div>
  );
}
