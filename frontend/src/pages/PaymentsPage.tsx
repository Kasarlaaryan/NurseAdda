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
import { CreditCard, DollarSign, ArrowUpRight, CheckCircle2, FileText, RotateCcw } from 'lucide-react';

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

      {/* Company & GST Info */}
      <Card className="border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/50">
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-amber-500/10 text-amber-500">
                <FileText className="w-5 h-5" />
              </div>
              <div>
                <p className="text-sm font-extrabold text-slate-900 dark:text-white">viewads</p>
                <p className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Healthcare Workforce Management</p>
              </div>
            </div>
            <div className="flex items-center gap-6">
              <div className="text-right">
                <p className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">GSTIN</p>
                <p className="text-xs font-black text-slate-900 dark:text-white font-mono">36FQNPS3757Q1ZK</p>
              </div>
              <div className="text-right">
                <p className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">GST Rate</p>
                <p className="text-xs font-black text-amber-600 dark:text-amber-400">18%</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

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

        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Total Transactions</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  {payments.length}
                </h3>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-500/10 text-amber-600">
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
                <TableHead>Refund</TableHead>
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
                      <p className="text-[10px] text-slate-400">Subtotal: ₹{(pmt.baseAmount + pmt.overtimeAmount).toLocaleString()}</p>
                      <p className="text-[10px] text-slate-400">GST (18%): ₹{((pmt.baseAmount + pmt.overtimeAmount) * 0.18).toFixed(2)}</p>
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
                      {pmt.refunded ? (
                        <div className="space-y-0.5">
                          <div className="flex items-center gap-1">
                            <RotateCcw className="w-3 h-3 text-emerald-500" />
                            <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400">Refunded</span>
                          </div>
                          <p className="text-[10px] text-slate-500">{pmt.refundPercentage}% → ₹{pmt.refundAmount?.toLocaleString()}</p>
                          {pmt.razorpayRefundId && (
                            <p className="text-[9px] text-slate-400 font-mono truncate max-w-[120px]" title={pmt.razorpayRefundId}>
                              {pmt.razorpayRefundId}
                            </p>
                          )}
                        </div>
                      ) : (
                        <span className="text-[10px] text-slate-400">—</span>
                      )}
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
