import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { Mail, Loader2 } from 'lucide-react';

export default function VerifyEmailPage() {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [timer, setTimer] = useState(60);
  const refs = useRef([]);

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    if (user.emailVerified) { navigate('/'); return; }
    const interval = setInterval(() => setTimer(t => t > 0 ? t - 1 : 0), 1000);
    return () => clearInterval(interval);
  }, [user, navigate]);

  const handleChange = (i, val) => {
    if (!/^\d*$/.test(val)) return;
    const next = [...otp];
    next[i] = val.slice(-1);
    setOtp(next);
    if (val && i < 5) refs.current[i + 1]?.focus();
  };

  const handleKeyDown = (i, e) => {
    if (e.key === 'Backspace' && !otp[i] && i > 0) refs.current[i - 1]?.focus();
  };

  const handleVerify = async () => {
    const otpStr = otp.join('');
    if (otpStr.length !== 6) { toast.error('Enter 6-digit OTP'); return; }
    setLoading(true);
    try {
      const { data } = await api.post('/auth/verify-email', { email: user.email, otp: otpStr });
      const updated = { ...user, emailVerified: true };
      localStorage.setItem('user', JSON.stringify(updated));
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      setUser(updated);
      toast.success('Email verified successfully!');
      navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Verification failed');
      setOtp(['', '', '', '', '', '']);
      refs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    try {
      await api.post('/auth/resend-verification', { email: user?.email });
      setTimer(60);
      toast.success('New OTP sent to your email');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to resend');
    }
  };

  if (!user) return null;

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md text-center">
        <Mail className="w-16 h-16 text-[#1e3a5f] mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-[#1e3a5f] mb-2">Verify Your Email</h1>
        <p className="text-gray-500 text-sm mb-1">We sent a 6-digit code to</p>
        <p className="text-[#1e3a5f] font-semibold mb-6">{user.email}</p>

        <div className="flex justify-center gap-3 mb-6">
          {otp.map((d, i) => (
            <input key={i} ref={el => refs.current[i] = el} value={d}
              onChange={e => handleChange(i, e.target.value)}
              onKeyDown={e => handleKeyDown(i, e)}
              maxLength={1}
              className="w-12 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-[#1e3a5f] focus:outline-none" />
          ))}
        </div>

        <button onClick={handleVerify} disabled={loading || otp.join('').length !== 6}
          className="w-full bg-[#1e3a5f] text-white py-3 rounded-lg hover:bg-[#2a4d7a] transition font-semibold disabled:opacity-50 cursor-pointer border-none mb-4">
          {loading ? <Loader2 className="w-5 h-5 animate-spin mx-auto" /> : 'Verify Email'}
        </button>

        <div className="text-sm text-gray-500">
          {timer > 0 ? (
            <p>Resend OTP in <span className="font-mono font-bold text-[#1e3a5f]">{timer}s</span></p>
          ) : (
            <button onClick={handleResend}
              className="text-[#1e3a5f] font-semibold hover:underline bg-transparent border-none cursor-pointer">
              Resend OTP
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
