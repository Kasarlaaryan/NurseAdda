import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Input } from '../components/common/Input';
import { Button } from '../components/common/Button';
import { useToast } from '../components/common/Toast';
import { Mail, ArrowLeft, CheckCircle } from 'lucide-react';

export const ForgotPasswordPage: React.FC = () => {
  const { showToast } = useToast();
  const [email, setEmail] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setSubmitted(true);
      showToast('info', 'Password Reset Email Sent', 'Check your inbox for instructions.');
    }, 800);
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-1">
        <h2 className="text-2xl font-black text-white tracking-tight">Recover Password</h2>
        <p className="text-xs text-slate-400">
          Enter your registered email address to receive a security reset link
        </p>
      </div>

      {submitted ? (
        <div className="p-6 rounded-2xl bg-teal-950/60 border border-teal-800 text-center space-y-3">
          <CheckCircle className="w-10 h-10 text-teal-400 mx-auto" />
          <h4 className="text-sm font-bold text-white">Reset Link Dispatched</h4>
          <p className="text-xs text-slate-300">
            We have dispatched password reset instructions to <span className="font-semibold text-teal-300">{email}</span>.
          </p>
          <Link to="/reset-password">
            <Button variant="outline" size="sm" className="mt-2 text-white border-slate-700">
              Proceed to Reset Password Page
            </Button>
          </Link>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Corporate / Hospital Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            leftIcon={<Mail className="w-4 h-4" />}
            className="bg-slate-800 text-white border-slate-700"
          />

          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full bg-teal-500 hover:bg-teal-400 text-slate-950 font-extrabold"
            isLoading={isLoading}
          >
            Send Password Reset Link
          </Button>
        </form>
      )}

      <div className="text-center">
        <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-teal-400 font-semibold hover:underline">
          <ArrowLeft className="w-3.5 h-3.5" />
          Back to Login
        </Link>
      </div>
    </div>
  );
};
