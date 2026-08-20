import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from '../components/common/Input';
import { Button } from '../components/common/Button';
import { useToast } from '../components/common/Toast';
import { Lock, ArrowLeft } from 'lucide-react';

export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      showToast('error', 'Passwords Do Not Match', 'Please ensure both passwords are identical.');
      return;
    }
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      showToast('success', 'Password Updated Successfully', 'You can now log in with your new password.');
      navigate('/login');
    }, 800);
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-1">
        <h2 className="text-2xl font-black text-white tracking-tight">Set New Password</h2>
        <p className="text-xs text-slate-400">Choose a robust security password for your account</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="New Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          leftIcon={<Lock className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <Input
          label="Confirm New Password"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
          leftIcon={<Lock className="w-4 h-4" />}
          className="bg-slate-800 text-white border-slate-700"
        />

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full bg-teal-500 hover:bg-teal-400 text-slate-950 font-extrabold"
          isLoading={isLoading}
        >
          Update Password & Sign In
        </Button>
      </form>

      <div className="text-center">
        <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-teal-400 font-semibold hover:underline">
          <ArrowLeft className="w-3.5 h-3.5" />
          Back to Login
        </Link>
      </div>
    </div>
  );
};
