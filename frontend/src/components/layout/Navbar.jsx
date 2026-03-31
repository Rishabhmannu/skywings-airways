import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Plane, User, LogOut, LayoutDashboard } from 'lucide-react';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-[#1e3a5f] text-white shadow-lg sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2 text-xl font-bold no-underline text-white">
            <Plane className="w-6 h-6" />
            SkyWings Airways
          </Link>

          <div className="flex items-center gap-6">
            <Link to="/" className="hover:text-blue-200 transition no-underline text-white text-sm">Home</Link>
            <Link to="/about" className="hover:text-blue-200 transition no-underline text-white text-sm">About</Link>

            {user ? (
              <>
                {user.role === 'ADMIN' ? (
                  <Link to="/admin" className="hover:text-blue-200 transition no-underline text-white text-sm flex items-center gap-1">
                    <LayoutDashboard className="w-4 h-4" /> Dashboard
                  </Link>
                ) : (
                  <>
                    <Link to="/my-bookings" className="hover:text-blue-200 transition no-underline text-white text-sm">My Bookings</Link>
                  </>
                )}
                <Link to="/profile" className="hover:text-blue-200 transition no-underline text-white text-sm flex items-center gap-1">
                  <User className="w-4 h-4" /> {user.name}
                </Link>
                <button onClick={handleLogout} className="hover:text-red-300 transition flex items-center gap-1 text-sm bg-transparent border-none text-white cursor-pointer">
                  <LogOut className="w-4 h-4" /> Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="hover:text-blue-200 transition no-underline text-white text-sm">Login</Link>
                <Link to="/signup" className="bg-white text-[#1e3a5f] px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-50 transition no-underline">
                  Sign Up
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
