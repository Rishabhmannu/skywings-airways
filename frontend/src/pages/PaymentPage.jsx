import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import toast from 'react-hot-toast';
import { CreditCard, Shield, Loader2, CheckCircle } from 'lucide-react';

export default function PaymentPage() {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [phase, setPhase] = useState('payment'); // payment | otp | success
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const [method, setMethod] = useState('CREDIT_CARD');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [otpInfo, setOtpInfo] = useState(null);
  const [timer, setTimer] = useState(300);
  const [loading, setLoading] = useState(false);
  const otpRefs = useRef([]);

  useEffect(() => {
    api.get(`/bookings/${bookingId}`).then(r => setBooking(r.data)).catch(() => toast.error('Booking not found'));
  }, [bookingId]);

  useEffect(() => {
    if (phase !== 'otp') return;
    const interval = setInterval(() => setTimer(t => t > 0 ? t - 1 : 0), 1000);
    return () => clearInterval(interval);
  }, [phase]);

  const formatCard = (val) => val.replace(/\D/g, '').replace(/(.{4})/g, '$1 ').trim().slice(0, 19);
  const formatExpiry = (val) => {
    const digits = val.replace(/\D/g, '').slice(0, 4);
    return digits.length > 2 ? digits.slice(0, 2) + '/' + digits.slice(2) : digits;
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await api.post('/payments/initiate', {
        bookingId: Number(bookingId),
        cardNumber: cardNumber.replace(/\s/g, ''),
        expiryDate: expiry,
        cvv,
        paymentMethod: method,
      });
      setOtpInfo(data);
      setPhase('otp');
      setTimer(300);
      toast.success('OTP sent to your phone & email!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Payment initiation failed');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpChange = (i, val) => {
    if (!/^\d*$/.test(val)) return;
    const next = [...otp];
    next[i] = val.slice(-1);
    setOtp(next);
    if (val && i < 5) otpRefs.current[i + 1]?.focus();
  };

  const handleOtpKeyDown = (i, e) => {
    if (e.key === 'Backspace' && !otp[i] && i > 0) otpRefs.current[i - 1]?.focus();
  };

  const handleVerify = async () => {
    const otpStr = otp.join('');
    if (otpStr.length !== 6) { toast.error('Enter 6-digit OTP'); return; }
    setLoading(true);
    try {
      await api.post('/payments/verify-otp', { bookingId: Number(bookingId), otp: otpStr });
      setPhase('success');
      toast.success('Payment verified! Booking confirmed!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'OTP verification failed');
      setOtp(['', '', '', '', '', '']);
      otpRefs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async (channel) => {
    try {
      await api.post('/payments/resend-otp', { bookingId: Number(bookingId), channel });
      setTimer(300);
      toast.success(`OTP resent via ${channel}`);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Resend failed');
    }
  };

  if (!booking) return <div className="flex justify-center py-20"><Loader2 className="w-8 h-8 animate-spin text-[#1e3a5f]" /></div>;

  return (
    <div className="max-w-lg mx-auto px-4 py-8">
      {/* Success */}
      {phase === 'success' && (
        <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
          <CheckCircle className="w-20 h-20 text-green-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-green-600 mb-2">Booking Confirmed!</h1>
          <p className="text-gray-500 mb-2">Transaction: {otpInfo?.transactionId}</p>
          <p className="text-gray-500 mb-6">A confirmation email with your e-ticket has been sent.</p>
          <div className="flex gap-3 justify-center">
            <button onClick={() => navigate('/my-bookings')}
              className="px-6 py-3 bg-[#1e3a5f] text-white rounded-lg font-semibold cursor-pointer border-none hover:bg-[#2a4d7a]">
              View My Bookings
            </button>
            <a href={`/api/tickets/${bookingId}/eticket`} target="_blank" rel="noreferrer"
              className="px-6 py-3 border-2 border-[#1e3a5f] text-[#1e3a5f] rounded-lg font-semibold no-underline hover:bg-blue-50">
              Download E-Ticket
            </a>
          </div>
        </div>
      )}

      {/* Payment Form */}
      {phase === 'payment' && (
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="text-center mb-6">
            <CreditCard className="w-12 h-12 text-[#1e3a5f] mx-auto mb-2" />
            <h1 className="text-2xl font-bold text-[#1e3a5f]">Payment</h1>
            <p className="text-3xl font-bold text-[#1e3a5f] mt-2">{'\u20B9'}{Number(booking.totalPrice).toLocaleString('en-IN')}</p>
            <p className="text-xs text-gray-400 mt-1">(incl. {'\u20B9'}{Number(booking.taxAmount).toLocaleString('en-IN')} tax)</p>
          </div>

          <div className="flex gap-2 mb-6">
            {['CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING'].map(m => (
              <button key={m} onClick={() => setMethod(m)}
                className={`flex-1 py-2 rounded-lg text-xs font-medium cursor-pointer border transition ${method === m ? 'bg-[#1e3a5f] text-white border-[#1e3a5f]' : 'bg-white text-gray-600 border-gray-300 hover:border-gray-400'}`}>
                {m.replace('_', ' ')}
              </button>
            ))}
          </div>

          <form onSubmit={handlePayment} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Card Number</label>
              <input value={cardNumber} onChange={e => setCardNumber(formatCard(e.target.value))} required placeholder="4532 0151 1283 0366"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none font-mono text-lg tracking-wider" />
              <p className="text-xs text-gray-400 mt-1">Test: 4532015112830366 or 4111111111111111</p>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">Expiry</label>
                <input value={expiry} onChange={e => setExpiry(formatExpiry(e.target.value))} required placeholder="MM/YY"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1">CVV</label>
                <input type="password" value={cvv} onChange={e => setCvv(e.target.value.replace(/\D/g, '').slice(0, 4))} required placeholder="***" maxLength={4}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none" />
              </div>
            </div>
            <div className="flex items-center gap-2 text-xs text-gray-400 bg-gray-50 p-3 rounded-lg">
              <Shield className="w-4 h-4" />
              Simulated payment — no real money will be charged
            </div>
            <button type="submit" disabled={loading}
              className="w-full bg-[#1e3a5f] text-white py-3 rounded-lg hover:bg-[#2a4d7a] transition font-semibold disabled:opacity-50 cursor-pointer border-none text-lg">
              {loading ? 'Processing...' : 'Proceed to Verify'}
            </button>
          </form>
        </div>
      )}

      {/* OTP Verification */}
      {phase === 'otp' && (
        <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
          <Shield className="w-12 h-12 text-[#1e3a5f] mx-auto mb-2" />
          <h1 className="text-2xl font-bold text-[#1e3a5f] mb-2">Verify Payment</h1>
          <p className="text-gray-500 text-sm mb-1">OTP sent via: {otpInfo?.otpSentVia?.join(' & ')}</p>
          <p className="text-gray-400 text-xs mb-6">Card: ****{otpInfo?.cardLastFour}</p>

          <div className="flex justify-center gap-3 mb-4">
            {otp.map((d, i) => (
              <input key={i} ref={el => otpRefs.current[i] = el} value={d}
                onChange={e => handleOtpChange(i, e.target.value)}
                onKeyDown={e => handleOtpKeyDown(i, e)}
                maxLength={1}
                className="w-12 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-[#1e3a5f] focus:outline-none" />
            ))}
          </div>

          <p className="text-sm text-gray-400 mb-4">
            Expires in: <span className="font-mono font-bold text-[#1e3a5f]">{Math.floor(timer / 60)}:{String(timer % 60).padStart(2, '0')}</span>
          </p>

          <button onClick={handleVerify} disabled={loading || otp.join('').length !== 6}
            className="w-full bg-[#1e3a5f] text-white py-3 rounded-lg hover:bg-[#2a4d7a] transition font-semibold disabled:opacity-50 cursor-pointer border-none mb-4">
            {loading ? 'Verifying...' : 'Verify & Confirm Booking'}
          </button>

          <div className="flex justify-center gap-4 text-sm">
            <button onClick={() => handleResend('SMS')} className="text-[#1e3a5f] hover:underline bg-transparent border-none cursor-pointer">Resend via SMS</button>
            <button onClick={() => handleResend('EMAIL')} className="text-[#1e3a5f] hover:underline bg-transparent border-none cursor-pointer">Resend via Email</button>
          </div>
        </div>
      )}
    </div>
  );
}
