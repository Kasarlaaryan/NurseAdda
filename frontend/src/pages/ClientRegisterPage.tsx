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
  Building2,
  ArrowRight,
  CheckCircle2,
  ShieldCheck,
  Smartphone,
  KeyRound,
  ChevronLeft,
} from 'lucide-react';

type Step = 1 | 2;

export const ClientRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { registerClient, verifyOtp, resendOtp } = useAuthStore();
  const { showToast } = useToast();

  const [step, setStep] = useState<Step>(1);
  const [isLoading, setIsLoading] = useState(false);

  // Form State
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    mobileNumber: '',
    organizationName: '',
    password: '',
    confirmPassword: '',
  });

  // OTP State
  const [otp, setOtp] = useState('');
  const [registeredEmail, setRegisteredEmail] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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
      await registerClient({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        mobileNumber: formData.mobileNumber,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
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
        <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center mx-auto mb-4">
          <Building2 className="w-7 h-7" />
        </div>
        <h2 className="text-2xl font-black text-white tracking-tight">Facility Partner Registration</h2>
        <p className="text-xs text-slate-400">Register your hospital or clinic to access verified healthcare staff</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Organization Name"
          name="organizationName"
          value={formData.organizationName}
          onChange={handleInputChange}
          placeholder="e.g. City General Hospital"
          required
          leftIcon={<Building2 className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Contact Person First Name"
            name="firstName"
            value={formData.firstName}
            onChange={handleInputChange}
            placeholder="John"
            required
            leftIcon={<User className="w-4 h-4" />}
            className="bg-slate-800 text-white border-slate-700"
          />
          <Input
            label="Contact Person Last Name"
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
          label="Work Email Address"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleInputChange}
          placeholder="admin@hospital.org"
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
          className="w-full bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-extrabold"
          isLoading={isLoading}
          rightIcon={<ArrowRight className="w-4 h-4" />}
        >
          Create Facility Account
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
        <span>HIPAA Compliant Platform</span>
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
        <div className="w-16 h-16 rounded-2xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center mx-auto">
          <KeyRound className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-black text-white tracking-tight">Verify Your Email</h2>
        <p className="text-xs text-slate-400">
          We sent a 6-digit OTP to <span className="text-emerald-400 font-semibold">{registeredEmail}</span>
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
          className="w-full bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-extrabold"
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
            className="text-emerald-400 font-bold hover:underline"
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
