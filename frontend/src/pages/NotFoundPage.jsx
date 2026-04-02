import { Link } from 'react-router-dom';
import { Plane } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4">
      <div className="text-center">
        <Plane className="w-16 h-16 text-gray-300 mx-auto mb-4 rotate-45" />
        <h1 className="text-6xl font-bold text-[#1e3a5f] mb-2">404</h1>
        <p className="text-xl text-gray-500 mb-2">Flight not found</p>
        <p className="text-sm text-gray-400 mb-8">The page you're looking for doesn't exist or has been moved.</p>
        <Link to="/"
          className="bg-[#1e3a5f] text-white px-6 py-3 rounded-lg font-semibold hover:bg-[#2a4d7a] transition no-underline inline-block">
          Back to Home
        </Link>
      </div>
    </div>
  );
}
