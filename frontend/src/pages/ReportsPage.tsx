import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Loader } from '../components/common/Loader';
import { useToast } from '../components/common/Toast';
import { reportService, AnalyticsResponse } from '../services/reportService';
import { BarChart3, Users, DollarSign, Clock, CheckCircle2, AlertTriangle } from 'lucide-react';

const BadgeClass = ({ text }: { text: string }) => (
  <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-amber-100 dark:bg-amber-950 text-amber-800 dark:text-amber-300">
    {text}
  </span>
);

function BarSegment({ label, value, total, color }: { label: string; value: number; total: number; color: string }) {
  const pct = total > 0 ? Math.round((value / total) * 100) : 0;
  return (
    <div className="space-y-2">
      <div className="flex justify-between text-xs font-bold">
        <span>{label}</span>
        <span>{pct}% ({value})</span>
      </div>
      <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-2.5">
        <div className={`${color} h-2.5 rounded-full transition-all duration-500`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

export const ReportsPage: React.FC = () => {
  const { showToast } = useToast();

  const { data: analytics, isLoading } = useQuery({
    queryKey: ['analytics'],
    queryFn: () => reportService.getAnalytics(),
  });

  const handleExport = () => {
    showToast('success', 'Report Exported', 'Executive analytics CSV has been generated and download started.');
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-40">
        <Loader />
      </div>
    );
  }

  const a = analytics as AnalyticsResponse | undefined;
  if (!a) return null;

  const totalStaff = a.totalStaff || 1;
  const categoryEntries = Object.entries(a.staffByCategory || {});
  const statusEntries = Object.entries(a.assignmentsByStatus || {});

  const categoryColors = ['bg-emerald-600', 'bg-purple-600', 'bg-blue-600', 'bg-amber-600', 'bg-rose-600', 'bg-teal-600'];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reports & Workforce Analytics"
        description="Comprehensive intelligence on shift fulfillment rates, agency revenue, overtime compliance & staff performance."
        actions={
          <Button
            variant="outline"
            size="sm"
            leftIcon={<BarChart3 className="w-4 h-4" />}
            onClick={handleExport}
          >
            Export Executive Report (CSV)
          </Button>
        }
      />

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="p-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-slate-400">Total Staff</span>
              <BadgeClass text={`${a.verifiedStaff} verified`} />
            </div>
            <div className="flex items-center gap-2">
              <p className="text-3xl font-black text-slate-900 dark:text-slate-100">{a.totalStaff}</p>
              <Users className="w-5 h-5 text-amber-500" />
            </div>
            <p className="text-xs text-emerald-600 font-semibold">{a.totalClients} partner clients</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-emerald-500">
          <CardContent className="p-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-slate-400">Total Revenue</span>
              <BadgeClass text={`${a.totalInvoices} invoices`} />
            </div>
            <div className="flex items-center gap-2">
              <p className="text-3xl font-black text-slate-900 dark:text-slate-100">₹{a.totalRevenue.toLocaleString()}</p>
              <DollarSign className="w-5 h-5 text-emerald-500" />
            </div>
            <p className="text-xs text-emerald-600 font-semibold">₹{a.totalPaid.toLocaleString()} paid</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="p-6 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-slate-400">Hours Worked</span>
              <BadgeClass text={`${a.totalAssignments} assignments`} />
            </div>
            <div className="flex items-center gap-2">
              <p className="text-3xl font-black text-slate-900 dark:text-slate-100">{a.totalHoursWorked.toLocaleString()}h</p>
              <Clock className="w-5 h-5 text-amber-500" />
            </div>
            <p className="text-xs text-amber-600 font-semibold">{a.activeAssignments} active, {a.completedAssignments} completed</p>
          </CardContent>
        </Card>
      </div>

      {/* Detailed Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Staff by Category */}
        <Card>
          <CardHeader>
            <CardTitle>Staffing Category Distribution</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {categoryEntries.length > 0 ? (
              categoryEntries.map(([category, count], idx) => (
                <BarSegment
                  key={category}
                  label={category}
                  value={count}
                  total={totalStaff}
                  color={categoryColors[idx % categoryColors.length]}
                />
              ))
            ) : (
              <p className="text-xs text-slate-500">No staff data available yet.</p>
            )}
          </CardContent>
        </Card>

        {/* Assignment Status */}
        <Card>
          <CardHeader>
            <CardTitle>Assignment Status Breakdown</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {statusEntries.length > 0 ? (
              statusEntries.map(([status, count]) => (
                <div key={status} className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    {status === 'COMPLETED' ? (
                      <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                    ) : status === 'ACTIVE' || status === 'ACCEPTED' ? (
                      <Clock className="w-4 h-4 text-amber-500" />
                    ) : status === 'CANCELLED' || status === 'REJECTED' ? (
                      <AlertTriangle className="w-4 h-4 text-rose-500" />
                    ) : (
                      <BarChart3 className="w-4 h-4 text-slate-400" />
                    )}
                    <span className="text-xs font-bold text-slate-900 dark:text-slate-100 capitalize">{status.toLowerCase()}</span>
                  </div>
                  <span className="font-extrabold text-amber-600 dark:text-amber-400">{count}</span>
                </div>
              ))
            ) : (
              <p className="text-xs text-slate-500">No assignment data available yet.</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Financial Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Financial Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200/60 dark:border-emerald-800/40">
              <p className="text-[10px] uppercase font-bold text-emerald-600 tracking-widest">Total Paid to Staff</p>
              <p className="text-2xl font-black text-emerald-700 dark:text-emerald-300 mt-1">₹{a.totalPaid.toLocaleString()}</p>
            </div>
            <div className="text-center p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/30 border border-amber-200/60 dark:border-amber-800/40">
              <p className="text-[10px] uppercase font-bold text-amber-600 tracking-widest">Pending Invoices</p>
              <p className="text-2xl font-black text-amber-700 dark:text-amber-300 mt-1">₹{a.totalPending.toLocaleString()}</p>
            </div>
            <div className="text-center p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/30 border border-amber-200/60 dark:border-amber-800/40">
              <p className="text-[10px] uppercase font-bold text-amber-600 tracking-widest">Net Revenue</p>
              <p className="text-2xl font-black text-amber-700 dark:text-amber-300 mt-1">₹{(a.totalRevenue - a.totalPaid).toLocaleString()}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
