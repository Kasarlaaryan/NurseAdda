import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { useToast } from '../components/common/Toast';
import { authService } from '../services/authService';
import {
  UserCog,
  Shield,
  ShieldCheck,
  Mail,
  Lock,
  User,
  Smartphone,
  ArrowRight,
  CheckCircle2,
  ChevronLeft,
} from 'lucide-react';

export const AdminRegisterPage: React.FC = () => {
  const { showToast } = useToast();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    adminType: 'ADMIN' as 'ADMIN' | 'SUPER_ADMIN',
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const registerMutation = useMutation({
    mutationFn: (data: typeof formData) => authService.registerAdmin(data),
    onSuccess: () => {
      showToast('success', 'Admin Created', `New ${formData.adminType.replace('_', ' ').toLowerCase()} account has been created.`);
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '',
        adminType: 'ADMIN',
      });
    },
    onError: (err: unknown) => {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Failed to create admin account.';
      showToast('error', 'Creation Failed', message);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      showToast('error', 'Password Mismatch', 'Your passwords do not match.');
      return;
    }

    if (formData.password.length < 6) {
      showToast('error', 'Weak Password', 'Password must be at least 6 characters.');
      return;
    }

    registerMutation.mutate(formData);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Create Admin Account"
        description="Provision a new administrator or super administrator portal account."
        actions={
          <Link to="/users">
            <Button variant="ghost" size="sm" leftIcon={<ChevronLeft className="w-4 h-4" />}>
              Back to Users
            </Button>
          </Link>
        }
      />

      <div className="max-w-2xl mx-auto">
        <Card>
          <CardContent className="p-8">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-12 h-12 rounded-2xl bg-purple-50 dark:bg-purple-500/10 flex items-center justify-center text-purple-600 dark:text-purple-400">
                <UserCog className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-lg font-black text-slate-900 dark:text-white">New Admin Account</h3>
                <p className="text-xs text-slate-500">Fill in the details to provision a new admin user</p>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              {/* Admin Type Selector */}
              <div>
                <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-2">
                  Account Role
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <button
                    type="button"
                    onClick={() => setFormData((prev) => ({ ...prev, adminType: 'ADMIN' }))}
                    className={`p-4 rounded-2xl border-2 text-left transition-all ${
                      formData.adminType === 'ADMIN'
                        ? 'border-amber-500 bg-amber-50 dark:bg-amber-950/40'
                        : 'border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <Shield className={`w-5 h-5 ${formData.adminType === 'ADMIN' ? 'text-amber-600' : 'text-slate-400'}`} />
                      <div>
                        <p className={`text-sm font-black ${formData.adminType === 'ADMIN' ? 'text-amber-700 dark:text-amber-300' : 'text-slate-900 dark:text-slate-100'}`}>
                          Admin
                        </p>
                        <p className="text-[10px] text-slate-500">Staff, clients, assignments</p>
                      </div>
                    </div>
                  </button>

                  <button
                    type="button"
                    onClick={() => setFormData((prev) => ({ ...prev, adminType: 'SUPER_ADMIN' }))}
                    className={`p-4 rounded-2xl border-2 text-left transition-all ${
                      formData.adminType === 'SUPER_ADMIN'
                        ? 'border-purple-500 bg-purple-50 dark:bg-purple-950/40'
                        : 'border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <ShieldCheck className={`w-5 h-5 ${formData.adminType === 'SUPER_ADMIN' ? 'text-purple-600' : 'text-slate-400'}`} />
                      <div>
                        <p className={`text-sm font-black ${formData.adminType === 'SUPER_ADMIN' ? 'text-purple-700 dark:text-purple-300' : 'text-slate-900 dark:text-slate-100'}`}>
                          Super Admin
                        </p>
                        <p className="text-[10px] text-slate-500">Full system access</p>
                      </div>
                    </div>
                  </button>
                </div>
              </div>

              {/* Name Fields */}
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="First Name"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  placeholder="John"
                  required
                  leftIcon={<User className="w-4 h-4" />}
                />
                <Input
                  label="Last Name"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  placeholder="Doe"
                  required
                  leftIcon={<User className="w-4 h-4" />}
                />
              </div>

              {/* Contact Fields */}
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Email Address"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="admin@nurseadda.com"
                  required
                  leftIcon={<Mail className="w-4 h-4" />}
                />
                <Input
                  label="Phone Number"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="+919876543210"
                  required
                  leftIcon={<Smartphone className="w-4 h-4" />}
                />
              </div>

              {/* Password Fields */}
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Password"
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  required
                  leftIcon={<Lock className="w-4 h-4" />}
                />
                <Input
                  label="Confirm Password"
                  name="confirmPassword"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  required
                  leftIcon={<CheckCircle2 className="w-4 h-4" />}
                />
              </div>

              {/* Summary */}
              <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800">
                <div className="flex items-center gap-2 mb-2">
                  <Badge variant={formData.adminType === 'SUPER_ADMIN' ? 'danger' : 'warning'} size="sm">
                    {formData.adminType.replace('_', ' ')}
                  </Badge>
                </div>
                <p className="text-[11px] text-slate-500">
                  {formData.adminType === 'SUPER_ADMIN'
                    ? 'This account will have full system access including user management, settings, and admin creation.'
                    : 'This account will have access to staff management, client management, assignments, billing, and verifications.'}
                </p>
              </div>

              {/* Submit */}
              <div className="flex justify-end gap-3 pt-2">
                <Link to="/users">
                  <Button variant="ghost" size="sm" type="button">Cancel</Button>
                </Link>
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  isLoading={registerMutation.isPending}
                  rightIcon={<ArrowRight className="w-4 h-4" />}
                >
                  Create {formData.adminType === 'SUPER_ADMIN' ? 'Super Admin' : 'Admin'} Account
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
