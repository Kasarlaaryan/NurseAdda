import React, { useState } from 'react';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Tabs } from '../components/common/Tabs';
import { useToast } from '../components/common/Toast';
import { Settings, Shield, Mail, Database, Save, Lock } from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState('ROLES');

  const handleSaveSettings = () => {
    showToast('success', 'System Settings Saved', 'Role permissions & master configuration updated.');
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Settings & Master Data Management"
        description="Configure role permission matrices, master healthcare categories, email templates & HIPAA security policies."
        actions={
          <Button variant="primary" size="sm" leftIcon={<Save className="w-4 h-4" />} onClick={handleSaveSettings}>
            Save All Changes
          </Button>
        }
      />

      <Tabs
        tabs={[
          { id: 'ROLES', label: 'Roles & Permissions Matrix' },
          { id: 'MASTER', label: 'Master Data & Categories' },
          { id: 'TEMPLATES', label: 'Email & Notification Templates' },
          { id: 'SECURITY', label: 'HIPAA & Compliance' }
        ]}
        activeTab={activeTab}
        onChange={setActiveTab}
        variant="pills"
      />

      {activeTab === 'ROLES' && (
        <Card>
          <CardHeader>
            <CardTitle>Role Permission Matrix</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-xs">
            <div className="border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden">
              <table className="w-full text-left">
                <thead className="bg-slate-50 dark:bg-slate-800 font-bold border-b border-slate-200 dark:border-slate-800">
                  <tr>
                    <th className="p-3">Module Feature</th>
                    <th className="p-3">Super Admin</th>
                    <th className="p-3">Ops Admin</th>
                    <th className="p-3">Healthcare Staff</th>
                    <th className="p-3">Client Hospital</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                  <tr>
                    <td className="p-3 font-bold">Manage Staff Directory</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 text-slate-400">View Self</td>
                    <td className="p-3 text-slate-400">View Assigned</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-bold">Create Shift Requests</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 text-slate-400">No Access</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                  </tr>
                  <tr>
                    <td className="p-3 font-bold">Approve Invoices & Billing</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 text-slate-400">No Access</td>
                    <td className="p-3 font-semibold text-blue-600">View & Pay</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}

      {activeTab === 'SECURITY' && (
        <Card>
          <CardHeader>
            <CardTitle>HIPAA Compliance & Security Controls</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-4 rounded-xl bg-teal-50 dark:bg-teal-950/40 border border-teal-200 dark:border-teal-800 flex items-center justify-between">
              <div>
                <h4 className="font-bold text-sm text-teal-900 dark:text-teal-200">256-Bit Data Encryption At Rest</h4>
                <p className="text-xs text-teal-700 dark:text-teal-300">All healthcare credentials and timesheet records are encrypted.</p>
              </div>
              <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-teal-600 text-white">Active</span>
            </div>

            <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-800 flex items-center justify-between">
              <div>
                <h4 className="font-bold text-sm text-slate-900 dark:text-slate-100">Session Auto-Timeout Threshold</h4>
                <p className="text-xs text-slate-500">Automatically locks inactive portal after 15 minutes of non-activity.</p>
              </div>
              <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-slate-200 text-slate-800">15 Mins</span>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};
