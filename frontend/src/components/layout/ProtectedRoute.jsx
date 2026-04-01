import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute({ requiredRole }) {
  const { user, loading } = useAuth();

  if (loading) return <div className="flex justify-center items-center h-64">Loading...</div>;

  if (!user) return <Navigate to="/login" replace />;

  if (requiredRole && user.role !== requiredRole) return <Navigate to="/" replace />;

  // Passengers must verify email before accessing protected routes
  if (!requiredRole && user.role !== 'ADMIN' && !user.emailVerified) {
    return <Navigate to="/verify-email" replace />;
  }

  return <Outlet />;
}
