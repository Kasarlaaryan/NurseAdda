import React from 'react';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Avatar } from '../components/common/Avatar';
import { Badge } from '../components/common/Badge';
import { useAuthStore } from '../store/useAuthStore';
import { useToast } from '../components/common/Toast';
import { USER_ROLES_CONFIG } from '../constants';
import { User, Mail, Phone, Shield, Lock } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, activeRole } = useAuthStore();
  const { showToast } = useToast();
  const roleConfig = USER_ROLES_CONFIG[activeRole] || USER_ROLES_CONFIG['ROLE_SUPER_ADMIN'];

  const handleUpdate = (e: React.FormEvent) => {
    e.preventDefault();
    showToast('success', 'Profile Updated', 'Your user information has been updated.');
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Account & User Profile"
        description="Manage your personal details, role credentials & portal preferences."
      />

      <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
        <Card className="md:col-span-4 text-center">
          <CardContent className="p-6 space-y-4">
            <Avatar name={user?.name || 'User'} src={user?.avatarUrl} size="xl" className="mx-auto" />
            <div>
              <h3 className="font-extrabold text-base text-slate-900 dark:text-slate-100">{user?.name}</h3>
              <p className="text-xs text-slate-500 mt-0.5">{user?.email}</p>
              <div className="mt-2">
                <Badge variant="primary" size="md">{roleConfig.label}</Badge>
              </div>
            </div>
            <div className="pt-4 border-t border-slate-100 dark:border-slate-800 text-xs text-left space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-500">Department:</span>
                <span className="font-semibold">{user?.department || 'Executive Operations'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Security Status:</span>
                <span className="font-semibold text-emerald-600">Verified & Active</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="md:col-span-8">
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleUpdate} className="space-y-4">
              <Input
                label="Full Name"
                defaultValue={user?.name}
                leftIcon={<User className="w-4 h-4" />}
                required
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Email Address"
                  type="email"
                  defaultValue={user?.email}
                  leftIcon={<Mail className="w-4 h-4" />}
                  required
                />
                <Input
                  label="Phone Number"
                  defaultValue={user?.phone || '+1 (555) 234-5678'}
                  leftIcon={<Phone className="w-4 h-4" />}
                  required
                />
              </div>
              <Button type="submit" variant="primary" size="sm" className="mt-2">
                Save Profile Changes
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
