import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useToast } from '../components/common/Toast';
import { Input } from '../components/common/Input';
import { Button } from '../components/common/Button';
import {
  Mail,
  Lock,
  User,
  Stethoscope,
  ArrowRight,
  CheckCircle2,
  ShieldCheck,
  Smartphone,
  KeyRound,
  ChevronLeft,
} from 'lucide-react';

type Step = 1 | 2;

const STAFF_CATEGORIES = [
  'Registered Nurse (RN)',
  'Nurse Practitioner (NP)',
  'Clinical Nurse Specialist (CNS)',
  'Certified Nurse Midwife (CNM)',
  'Licensed Practical Nurse (LPN)',
  'General Nursing and Midwifery (GNM)',
  'Auxiliary Nurse Midwifery (ANM)',
  'Physiotherapist',
  'Medical Lab Technician',
];

export const StaffRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { registerStaff, verifyOtp, resendOtp } = useAuthStore();
  const { showToast } = useToast();

  const [step, setStep] = useState<Step>(1);
  const [isLoading, setIsLoading] = useState(false);

  // Form State
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    mobileNumber: '',
    staffCategory: 'Registered Nurse (RN)',
    password: '',
    confirmPassword: '',
  });

  // OTP State
  const [otp, setOtp] = useState('');
  const [registeredEmail, setRegisteredEmail] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  /** Step 1: Submit registration form */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      showToast('error', 'Password Mismatch', 'Your passwords do not match.');
      return;
    }

    if (formData.password.length < 6) {
      showToast('error', 'Weak Password', 'Password must be at least 6 characters.');
      return;
    }

    setIsLoading(true);
    try {
      await registerStaff({
        fullName: `${formData.firstName} ${formData.lastName}`,
        email: formData.email,
        phone: formData.mobileNumber,
        staffCategory: formData.staffCategory,
        password: formData.password,
      });

      setRegisteredEmail(formData.email);
      showToast('success', 'Registration Successful', 'Please check your email for the OTP.');
      setStep(2);
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Registration failed. Please try again.';
      showToast('error', 'Registration Failed', message);
    } finally {
      setIsLoading(false);
    }
  };

  /** Step 2: Verify OTP */
  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();

    if (otp.length !== 6) {
      showToast('error', 'Invalid OTP', 'Please enter the 6-digit OTP.');
      return;
    }

    setIsLoading(true);
    try {
      await verifyOtp(registeredEmail, otp);
      showToast('success', 'Verified!', 'Your account has been verified. Please log in.');
      navigate('/login');
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Invalid OTP. Please try again.';
      showToast('error', 'Verification Failed', message);
    } finally {
      setIsLoading(false);
    }
  };

  /** Resend OTP */
  const handleResendOtp = async () => {
    try {
      await resendOtp(registeredEmail);
      showToast('success', 'OTP Resent', 'A new OTP has been sent to your email.');
    } catch {
      showToast('error', 'Failed', 'Could not resend OTP. Please try again.');
    }
  };

  // ─── Step 1: Registration Form ───────────────────────
  const renderStep1 = () => (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex items-center gap-3">
        <Link
          to="/register"
          className="text-[10px] uppercase font-black text-slate-500 hover:text-white transition-colors flex items-center gap-1"
        >
          <ChevronLeft className="w-3 h-3" /> Change Role
        </Link>
        <div className="h-px flex-1 bg-slate-800" />
      </div>

      <div className="text-center space-y-1">
        <div className="w-14 h-14 rounded-2xl bg-amber-500/10 text-amber-500 flex items-center justify-center mx-auto mb-4">
          <Stethoscope className="w-7 h-7" />
        </div>
        <h2 className="text-2xl font-black text-white tracking-tight">Healthcare Professional Registration</h2>
        <p className="text-xs text-slate-400">Join NurseAdda as a verified nurse, ANM, or technician</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <Input
            label="First Name"
            name="firstName"
            value={formData.firstName}
            onChange={handleInputChange}
            placeholder="John"
            required
            leftIcon={<User className="w-4 h-4" />}
            className="bg-slate-800 text-white border-slate-700"
          />
          <Input
            label="Last Name"
            name="lastName"
            value={formData.lastName}
            onChange={handleInputChange}
            placeholder="Doe"
            required
            leftIcon={<User className="w-4 h-4" />}
            className="bg-slate-800 text-white border-slate-700"
          />
        </div>

        <Input
          label="Email Address"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleInputChange}
          placeholder="name@work.com"
          required
          leftIcon={<Mail className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <Input
          label="Mobile Number"
          name="mobileNumber"
          value={formData.mobileNumber}
          onChange={handleInputChange}
          placeholder="+919876543210"
          required
          leftIcon={<Smartphone className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <div>
          <label className="text-xs font-semibold uppercase tracking-wider text-slate-400 block mb-1">
            Staff Category
          </label>
          <select
            name="staffCategory"
            value={formData.staffCategory}
            onChange={handleInputChange}
            className="w-full rounded-xl border border-slate-700 bg-slate-800 text-white text-sm p-2.5 focus:ring-1 focus:ring-amber-500 outline-none"
          >
            {STAFF_CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleInputChange}
            required
            leftIcon={<Lock className="w-4 h-4" />}
            className="bg-slate-800 text-white border-slate-700"
          />
          <Input
            label="Confirm Password"
            name="confirmPassword"
            type="password"
            value={formData.confirmPassword}
            onChange={handleInputChange}
            required
            leftIcon={<CheckCircle2 className="w-4 h-4" />}
            className="bg-slate-800 text-white border-slate-700"
          />
        </div>

        <div className="flex items-start gap-3 p-4 rounded-xl bg-slate-900/50 border border-slate-800">
          <div className="pt-0.5">
            <input
              type="checkbox"
              required
              className="rounded border-slate-700 bg-slate-800 text-amber-500 focus:ring-amber-500"
            />
          </div>
          <p className="text-[10px] text-slate-500 leading-relaxed">
            I agree to NurseAdda's{' '}
            <span className="text-amber-400 hover:underline cursor-pointer">Terms of Service</span> and{' '}
            <span className="text-amber-400 hover:underline cursor-pointer">Privacy Policy</span>.
          </p>
        </div>

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold"
          isLoading={isLoading}
          rightIcon={<ArrowRight className="w-4 h-4" />}
        >
          Create Staff Account
        </Button>

        <div className="text-center text-xs text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="text-amber-400 font-bold hover:underline">
            Log In
          </Link>
        </div>
      </form>

      <div className="pt-2 border-t border-slate-800/80 text-center text-[10px] text-slate-500 flex items-center justify-center gap-1.5 uppercase tracking-widest font-bold">
        <ShieldCheck className="w-4 h-4 text-emerald-500" />
        <span>Verified Credentialing Standards</span>
      </div>
    </div>
  );

  // ─── Step 2: OTP Verification ────────────────────────
  const renderStep2 = () => (
    <div className="space-y-6 animate-in fade-in slide-in-from-right-4 duration-500">
      <div className="flex items-center gap-3">
        <button
          onClick={() => setStep(1)}
          className="text-[10px] uppercase font-black text-slate-500 hover:text-white transition-colors"
        >
          &larr; Back
        </button>
        <div className="h-px flex-1 bg-slate-800" />
      </div>

      <div className="text-center space-y-2">
        <div className="w-16 h-16 rounded-2xl bg-amber-500/10 text-amber-500 flex items-center justify-center mx-auto">
          <KeyRound className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-black text-white tracking-tight">Verify Your Email</h2>
        <p className="text-xs text-slate-400">
          We sent a 6-digit OTP to <span className="text-amber-400 font-semibold">{registeredEmail}</span>
        </p>
      </div>

      <form onSubmit={handleVerifyOtp} className="space-y-4">
        <Input
          label="Enter OTP"
          value={otp}
          onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
          placeholder="000000"
          required
          leftIcon={<KeyRound className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700 text-center text-2xl tracking-[0.5em] font-mono"
          maxLength={6}
        />

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold"
          isLoading={isLoading}
          rightIcon={<ArrowRight className="w-4 h-4" />}
        >
          Verify & Complete
        </Button>

        <div className="text-center text-xs text-slate-500">
          Didn't receive the code?{' '}
          <button
            type="button"
            onClick={handleResendOtp}
            className="text-amber-400 font-bold hover:underline"
          >
            Resend OTP
          </button>
        </div>
      </form>
    </div>
  );

  return (
    <div className="max-w-md mx-auto">
      {step === 1 && renderStep1()}
      {step === 2 && renderStep2()}
    </div>
  );
};
