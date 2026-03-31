import { Plane } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-[#1e3a5f] text-white mt-auto">
      <div className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="flex items-center gap-2">
            <Plane className="w-5 h-5" />
            <span className="font-bold">SkyWings Airways</span>
          </div>
          <p className="text-blue-200 text-sm">
            Portfolio project — simulated booking system. No real payments are processed.
          </p>
          <p className="text-blue-300 text-xs">&copy; {new Date().getFullYear()} SkyWings Airways</p>
        </div>
      </div>
    </footer>
  );
}
