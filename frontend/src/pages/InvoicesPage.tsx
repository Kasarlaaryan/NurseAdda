import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Modal } from '../components/common/Modal';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { useToast } from '../components/common/Toast';
import { useAuthStore } from '../store/useAuthStore';
import { invoiceService, InvoiceResponse } from '../services/invoiceService';
import { billingService, BillingSummaryResponse } from '../services/invoiceService';
import { FileText, Download, Plus, Printer, CheckCircle2, DollarSign } from 'lucide-react';

/** Map backend status to display values */
function invoiceStatusDisplay(status: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    PENDING: { label: 'Pending', variant: 'warning' },
    PAID: { label: 'Paid', variant: 'success' },
    OVERDUE: { label: 'Overdue', variant: 'danger' },
    CANCELLED: { label: 'Cancelled', variant: 'danger' },
  };
  return map[status] || { label: status, variant: 'info' as const };
}

export const InvoicesPage: React.FC = () => {
  const { showToast } = useToast();
  const { user, activeRole } = useAuthStore();
  const [pdfInvoice, setPdfInvoice] = useState<InvoiceResponse | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['invoices'],
    queryFn: () => invoiceService.getAll(0, 50),
  });

  const { data: billingSummary } = useQuery({
    queryKey: ['billing-summary'],
    queryFn: () => billingService.getSummary(),
  });

  const invoices = data?.content || [];
  const summary = billingSummary as BillingSummaryResponse | undefined;

  const filteredInvoices = invoices.filter(inv => {
    const matchesSearch = 
      inv.staffName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      inv.designation.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesSearch;
  });

  const handleDownloadPdf = async (id: number) => {
    try {
      await invoiceService.downloadPdf(id);
    } catch {
      showToast('error', 'Download Failed', 'Could not download the invoice PDF.');
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={activeRole === 'ROLE_USER' ? 'My Billings & Invoices' : 'Invoices & Billing Hub'}
        description={activeRole === 'ROLE_USER' 
          ? 'Track your service billing history, download tax invoices, and monitor payment status.'
          : 'Automated billing, timesheet calculations, tax breakdown, and client PDF invoice generation.'
        }
      />

      {/* Financial Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Total Billed</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  ${(summary?.totalBilled || 0).toLocaleString()}
                </h3>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-500/10 text-amber-600">
                <DollarSign className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-emerald-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Total Paid</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  ${(summary?.totalPaid || 0).toLocaleString()}
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
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Pending / Overdue</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">
                  ${(summary?.totalPending || 0).toLocaleString()}
                </h3>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-500/10 text-amber-600">
                <FileText className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex flex-col md:flex-row gap-4 items-center justify-between bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800">
        <div className="relative w-full md:w-96">
          <FileText className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search by staff name or designation..."
            className="w-full pl-10 pr-4 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500/20"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Loader />
        </div>
      ) : filteredInvoices.length === 0 ? (
        <EmptyState
          title="No invoices found"
          description="Invoices will appear here once attendance is checked out and billing is generated."
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
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredInvoices.map((inv) => {
                const { label: statusLabel, variant } = invoiceStatusDisplay(inv.status);
                return (
                  <TableRow key={inv.id}>
                    <TableCell>
                      <p className="font-bold text-slate-900 dark:text-slate-100">{inv.staffName}</p>
                      <p className="text-[11px] text-slate-500">{inv.designation}</p>
                    </TableCell>
                    <TableCell>
                      <span className="text-xs">{inv.location}</span>
                    </TableCell>
                    <TableCell>
                      <p className="text-xs font-medium">{inv.baseHours}h base</p>
                      {inv.overtimeHours > 0 && (
                        <p className="text-[11px] text-amber-500 font-bold">+{inv.overtimeHours}h OT</p>
                      )}
                    </TableCell>
                    <TableCell>
                      <p className="font-extrabold text-amber-600 dark:text-amber-400">₹{inv.totalAmount.toLocaleString()}</p>
                      <p className="text-[11px] text-slate-400">Rate: ₹{inv.clientHourlyRate}/hr</p>
                    </TableCell>
                    <TableCell>
                      <Badge variant={variant} size="sm" dot>
                        {statusLabel}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <span className="text-[11px] text-slate-400">
                        {inv.createdAt ? new Date(inv.createdAt).toLocaleDateString() : '—'}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="outline"
                          size="sm"
                          leftIcon={<FileText className="w-3.5 h-3.5" />}
                          onClick={() => setPdfInvoice(inv)}
                        >
                          View
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          leftIcon={<Download className="w-3.5 h-3.5" />}
                          onClick={() => handleDownloadPdf(inv.id)}
                        >
                          PDF
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Card>
      )}

      {/* PDF Invoice Modal Preview */}
      {pdfInvoice && (
        <Modal
          isOpen={Boolean(pdfInvoice)}
          onClose={() => setPdfInvoice(null)}
          title={`Invoice #${pdfInvoice.id}`}
          maxWidth="2xl"
          footer={
            <div className="flex justify-end gap-2">
              <Button variant="outline" size="sm" onClick={() => setPdfInvoice(null)}>
                Close
              </Button>
              <Button
                variant="primary"
                size="sm"
                leftIcon={<Download className="w-3.5 h-3.5" />}
                onClick={() => handleDownloadPdf(pdfInvoice.id)}
              >
                Download PDF
              </Button>
            </div>
          }
        >
          <div className="p-6 space-y-4 text-slate-900 dark:text-slate-100">
            <div className="flex justify-between items-start border-b border-slate-100 dark:border-slate-800 pb-4">
              <div>
                <h2 className="text-2xl font-black text-amber-600">NurseAdda<span className="text-slate-400">WFM</span></h2>
                <p className="text-xs text-slate-500 font-medium">Enterprise Healthcare Staffing</p>
              </div>
              <div className="text-right">
                <p className="text-lg font-black">Invoice #{pdfInvoice.id}</p>
                <p className="text-xs text-slate-500 font-bold">
                  Created: {pdfInvoice.createdAt ? new Date(pdfInvoice.createdAt).toLocaleDateString() : '—'}
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-8 text-xs">
              <div className="space-y-1">
                <p className="font-black uppercase text-slate-400 text-[10px] tracking-widest">Staff Member</p>
                <p className="font-black text-sm">{pdfInvoice.staffName}</p>
                <p className="text-slate-500">{pdfInvoice.designation}</p>
              </div>
              <div className="space-y-1">
                <p className="font-black uppercase text-slate-400 text-[10px] tracking-widest">Location</p>
                <p className="font-bold">{pdfInvoice.location}</p>
              </div>
            </div>

            <div className="flex justify-end pt-4">
              <div className="w-72 space-y-2.5 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/40 border border-slate-100 dark:border-slate-800">                  <div className="flex justify-between text-xs">
                    <span className="font-bold text-slate-400 uppercase tracking-widest text-[9px]">Base ({pdfInvoice.baseHours}h × ₹{pdfInvoice.clientHourlyRate}):</span>
                    <span className="font-black">₹{pdfInvoice.baseAmount.toLocaleString()}</span>
                  </div>
                  {pdfInvoice.overtimeHours > 0 && (
                    <div className="flex justify-between text-xs">
                      <span className="font-bold text-slate-400 uppercase tracking-widest text-[9px]">Overtime ({pdfInvoice.overtimeHours}h):</span>
                      <span className="font-black">₹{pdfInvoice.overtimeAmount.toLocaleString()}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-xs">
                    <span className="font-bold text-slate-400 uppercase tracking-widest text-[9px]">Subtotal:</span>
                    <span className="font-black">₹{(pdfInvoice.subtotal || pdfInvoice.baseAmount + pdfInvoice.overtimeAmount).toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="font-bold text-slate-400 uppercase tracking-widest text-[9px]">GST (18%):</span>
                    <span className="font-black">₹{(pdfInvoice.gstAmount || 0).toLocaleString()}</span>
                  </div>
                  <div className="pt-2.5 mt-2.5 border-t border-slate-200 dark:border-slate-700 flex justify-between items-center">
                    <span className="font-black text-[10px] uppercase tracking-[0.2em] text-amber-600">Total (incl. GST)</span>
                    <span className="text-xl font-black text-amber-600">₹{pdfInvoice.totalAmount.toLocaleString()}</span>
                  </div>
                  <div className="pt-2 mt-2 border-t border-slate-200 dark:border-slate-700 text-center">
                    <p className="text-[9px] font-bold text-slate-500 uppercase tracking-wider">GSTIN: 36FQNPS3757Q1ZK</p>
                    <p className="text-[9px] font-bold text-slate-500 uppercase tracking-wider">Issued by: viewads</p>
                  </div>
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
