import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Tabs } from '../components/common/Tabs';
import { Badge } from '../components/common/Badge';
import { useToast } from '../components/common/Toast';
import { rateService, RateConfigResponse } from '../services/invoiceService';
import { Settings, Shield, DollarSign, Save, Clock, Sun, Moon, Zap } from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState('RATES');

  // ─── Fetch rate configs ──────────────────────────────
  const { data: ratesData, isLoading: ratesLoading } = useQuery({
    queryKey: ['rate-configs'],
    queryFn: () => rateService.getAll(),
  });

  // ─── Edit state for each rate config ─────────────────
  const [editRates, setEditRates] = useState<Record<string, {
    clientHourlyRate: string;
    staffHourlyRate: string;
    overtimeMultiplier: string;
  }>>({});

  const handleRateChange = (shiftType: string, field: string, value: string) => {
    setEditRates(prev => ({
      ...prev,
      [shiftType]: {
        ...prev[shiftType],
        [field]: value,
      },
    }));
  };

  // ─── Save mutation ───────────────────────────────────
  const saveMutation = useMutation({
    mutationFn: async (rates: RateConfigResponse[]) => {
      const results = await Promise.all(
        rates.map(rate => {
          const edit = editRates[rate.shiftType];
          return rateService.createOrUpdate({
            shiftType: rate.shiftType,
            clientHourlyRate: edit ? parseFloat(edit.clientHourlyRate) : rate.clientHourlyRate,
            staffHourlyRate: edit ? parseFloat(edit.staffHourlyRate) : rate.staffHourlyRate,
            overtimeMultiplier: edit ? parseFloat(edit.overtimeMultiplier) : rate.overtimeMultiplier,
          });
        })
      );
      return results;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rate-configs'] });
      setEditRates({});
      showToast('success', 'Rates Saved', 'All rate configurations have been updated.');
    },
    onError: () => {
      showToast('error', 'Failed', 'Could not save rate configurations.');
    },
  });

  const rates = (ratesData as RateConfigResponse[]) || [];

  const shiftConfig: Record<string, { label: string; icon: React.ElementType; description: string; color: string }> = {
    '8HR': { label: 'Day Shift (8hr)', icon: Sun, description: 'Standard 8-hour day shift rate', color: 'bg-blue-500' },
    '12HR': { label: 'Extended Shift (12hr)', icon: Clock, description: 'Extended 12-hour shift rate', color: 'bg-purple-500' },
    'NIGHT': { label: 'Night Shift', icon: Moon, description: 'Night shift premium rate', color: 'bg-indigo-500' },
    '24HR': { label: '24hr On-Call', icon: Zap, description: '24-hour on-call rate', color: 'bg-amber-500' },
  };

  const handleSaveAll = () => {
    if (rates.length > 0) {
      saveMutation.mutate(rates);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Settings & Master Data Management"
        description="Configure billing rates, role permissions & master healthcare categories."
        actions={
          <Button variant="primary" size="sm" leftIcon={<Save className="w-4 h-4" />} onClick={handleSaveAll}>
            Save All Changes
          </Button>
        }
      />

      <Tabs
        tabs={[
          { id: 'RATES', label: 'Billing Rates', count: rates.length },
          { id: 'ROLES', label: 'Roles & Permissions Matrix' },
          { id: 'SECURITY', label: 'HIPAA & Compliance' }
        ]}
        activeTab={activeTab}
        onChange={setActiveTab}
        variant="pills"
      />

      {/* ─── Billing Rates Tab ───────────────────────── */}
      {activeTab === 'RATES' && (
        <div className="space-y-6">
          <Card className="border-amber-200 dark:border-amber-800/30 bg-amber-50/50 dark:bg-amber-950/10">
            <CardContent className="p-4">
              <div className="flex items-start gap-3">
                <DollarSign className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
                <div className="text-xs text-amber-700 dark:text-amber-400 leading-relaxed">
                  <p className="font-bold mb-1">Super Admin Rate Configuration</p>
                  <p>Set client billing rates and staff pay rates per shift type. <strong>GST (18%)</strong> is automatically added to all invoices. Overtime is charged at the configured multiplier rate.</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {ratesLoading ? (
            <div className="text-center py-8 text-slate-400 text-sm">Loading rate configs...</div>
          ) : rates.length === 0 ? (
            <Card>
              <CardContent className="p-8 text-center">
                <DollarSign className="w-8 h-8 text-slate-300 mx-auto mb-3" />
                <p className="text-sm font-bold text-slate-500">No rate configs found</p>
                <p className="text-xs text-slate-400 mt-1">Rate configs will be auto-created on first startup.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {rates.map((rate) => {
                const config = shiftConfig[rate.shiftType] || { label: rate.shiftType, icon: Clock, description: '', color: 'bg-slate-500' };
                const Icon = config.icon;
                const edit = editRates[rate.shiftType];
                const hasChanges = Boolean(edit);

                return (
                  <Card key={rate.id} className={hasChanges ? 'ring-2 ring-amber-500/30' : ''}>
                    <CardHeader className="pb-2">
                      <div className="flex items-center justify-between">
                        <CardTitle className="flex items-center gap-2 text-sm">
                          <div className={`p-2 rounded-xl ${config.color} text-white`}>
                            <Icon className="w-4 h-4" />
                          </div>
                          {config.label}
                        </CardTitle>
                        {hasChanges && <Badge variant="warning" size="sm">Modified</Badge>}
                      </div>
                      <p className="text-[11px] text-slate-500 mt-1">{config.description}</p>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <Input
                        label="Client Billing Rate (₹/hr)"
                        type="number"
                        step="0.01"
                        min="0"
                        value={edit?.clientHourlyRate ?? rate.clientHourlyRate}
                        onChange={(e) => handleRateChange(rate.shiftType, 'clientHourlyRate', e.target.value)}
                      />
                      <Input
                        label="Staff Pay Rate (₹/hr)"
                        type="number"
                        step="0.01"
                        min="0"
                        value={edit?.staffHourlyRate ?? rate.staffHourlyRate}
                        onChange={(e) => handleRateChange(rate.shiftType, 'staffHourlyRate', e.target.value)}
                      />
                      <Input
                        label="Overtime Multiplier"
                        type="number"
                        step="0.25"
                        min="1"
                        max="5"
                        value={edit?.overtimeMultiplier ?? rate.overtimeMultiplier}
                        onChange={(e) => handleRateChange(rate.shiftType, 'overtimeMultiplier', e.target.value)}
                      />
                      <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 text-[10px] text-slate-500 space-y-1">
                        <p>Client pays: ₹{edit?.clientHourlyRate ?? rate.clientHourlyRate}/hr</p>
                        <p>Staff earns: ₹{edit?.staffHourlyRate ?? rate.staffHourlyRate}/hr</p>
                        <p>Overtime: {(edit?.overtimeMultiplier ?? rate.overtimeMultiplier)}x rate</p>
                        <p className="font-bold text-amber-600">+ 18% GST on all invoices</p>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ─── Roles Tab ───────────────────────────────── */}
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
                    <td className="p-3 font-bold">Configure Billing Rates</td>
                    <td className="p-3 font-semibold text-emerald-600">Full Access</td>
                    <td className="p-3 text-slate-400">No Access</td>
                    <td className="p-3 text-slate-400">No Access</td>
                    <td className="p-3 text-slate-400">No Access</td>
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

      {/* ─── Security Tab ────────────────────────────── */}
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
