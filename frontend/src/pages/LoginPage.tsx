import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useToast } from '../components/common/Toast';
import { Input } from '../components/common/Input';
import { Button } from '../components/common/Button';
import { Mail, Lock, ShieldCheck, ArrowRight } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const { showToast } = useToast();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await login(email, password);
      showToast('success', 'Login Successful', 'Welcome back!');
      navigate('/dashboard');
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Invalid credentials. Please try again.';
      showToast('error', 'Login Failed', message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto space-y-6">
      <div className="text-center space-y-1">
        <h2 className="text-2xl font-black text-white tracking-tight">Portal Access Sign In</h2>
        <p className="text-xs text-slate-400">Enter your NurseAdda account credentials</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Email Address"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          leftIcon={<Mail className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <Input
          label="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          leftIcon={<Lock className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <div className="flex items-center justify-between text-xs">
          <label className="flex items-center gap-2 text-slate-400 cursor-pointer select-none">
            <input type="checkbox" defaultChecked className="rounded border-slate-700 bg-slate-800 text-sky-500 focus:ring-sky-500" />
            <span>Remember device</span>
          </label>
          <Link to="/forgot-password" className="text-sky-400 hover:underline font-semibold">
            Forgot Password?
          </Link>
        </div>

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full bg-sky-500 hover:bg-sky-400 text-slate-950 font-extrabold"
          isLoading={isLoading}
          rightIcon={<ArrowRight className="w-4 h-4" />}
        >
          Sign In
        </Button>

        <div className="text-center text-xs text-slate-400">
          New to NurseAdda?{' '}
          <Link to="/register" className="text-sky-400 hover:underline font-bold">
            Create Account
          </Link>
        </div>
      </form>

      <div className="pt-2 border-t border-slate-800/80 text-center text-xs text-slate-500 flex items-center justify-center gap-1.5">
        <ShieldCheck className="w-4 h-4 text-sky-400" />
        <span>Encrypted SSL 256-Bit Connection</span>
      </div>
    </div>
  );
};
