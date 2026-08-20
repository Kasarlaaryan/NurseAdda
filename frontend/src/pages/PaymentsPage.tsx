import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { useAuthStore } from '../store/useAuthStore';
import { paymentService, PaymentResponse } from '../services/invoiceService';
import { CreditCard, DollarSign, ArrowUpRight, CheckCircle2 } from 'lucide-react';

/** Map backend status to display values */
function paymentStatusDisplay(status: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    PENDING: { label: 'Pending', variant: 'warning' },
    COMPLETED: { label: 'Completed', variant: 'success' },
    FAILED: { label: 'Failed', variant: 'danger' },
    REFUNDED: { label: 'Refunded', variant: 'danger' },
  };
  return map[status] || { label: status, variant: 'info' as const };
}

export const PaymentsPage: React.FC = () => {
  const { activeRole } = useAuthStore();

  const { data, isLoading } = useQuery({
    queryKey: ['payments'],
    queryFn: () => paymentService.getAll(0, 50),
  });

  const payments = data?.content || [];

  const totalPaid = payments
    .filter((p: PaymentResponse) => p.status === 'COMPLETED')
    .reduce((sum: number, p: PaymentResponse) => sum + p.totalAmount, 0);

  const totalPending = payments
    .filter((p: PaymentResponse) => p.status !== 'COMPLETED')
    .reduce((sum: number, p: PaymentResponse) => sum + p.totalAmount, 0);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Payment & Collections Tracker"
        description="Monitor staff payments, pending settlements, and completed payouts."
      />

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="border-l-4 border-l-emerald-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Total Paid</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  ₹{totalPaid.toLocaleString()}
                </h3>
              </div>
              <div className="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600">
                <CheckCircle2 className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Pending / Failed</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  ₹{totalPending.toLocaleString()}
                </h3>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-500/10 text-amber-600">
                <ArrowUpRight className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-sky-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Total Transactions</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  {payments.length}
                </h3>
              </div>
              <div className="p-3 rounded-xl bg-sky-50 dark:bg-sky-500/10 text-sky-600">
                <CreditCard className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Loader />
        </div>
      ) : payments.length === 0 ? (
        <EmptyState
          title="No payments found"
          description="Payments will appear here once staff attendance is checked out and billing is generated."
        />
      ) : (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Staff & Designation</TableHead>
                <TableHead>Location</TableHead>
                <TableHead>Hours (Base / OT)</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Razorpay Order</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {payments.map((pmt: PaymentResponse) => {
                const { label: statusLabel, variant } = paymentStatusDisplay(pmt.status);
                return (
                  <TableRow key={pmt.id}>
                    <TableCell>
                      <p className="font-bold text-slate-900 dark:text-slate-100">{pmt.designation}</p>
                      <p className="text-[11px] text-slate-500">Assignment #{pmt.assignmentId}</p>
                    </TableCell>
                    <TableCell>
                      <span className="text-xs">{pmt.location}</span>
                    </TableCell>
                    <TableCell>
                      <p className="text-xs font-medium">{pmt.baseHours}h base</p>
                      {pmt.overtimeHours > 0 && (
                        <p className="text-[11px] text-amber-500 font-bold">+{pmt.overtimeHours}h OT</p>
                      )}
                    </TableCell>
                    <TableCell>
                      <p className="font-bold text-emerald-600 dark:text-emerald-400">₹{pmt.totalAmount.toLocaleString()}</p>
                      <p className="text-[11px] text-slate-400">Rate: ₹{pmt.staffHourlyRate}/hr</p>
                    </TableCell>
                    <TableCell>
                      <span className="text-[10px] text-slate-400 font-mono">
                        {pmt.razorpayOrderId || '—'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={variant} size="sm" dot>
                        {statusLabel}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <span className="text-[11px] text-slate-400">
                        {pmt.createdAt ? new Date(pmt.createdAt).toLocaleDateString() : '—'}
                      </span>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Card>
      )}
    </div>
  );
};
