import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/useAuthStore';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Badge } from '../components/common/Badge';
import { Button } from '../components/common/Button';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import {
  assignmentService,
  staffingRequestService,
  AssignmentResponse,
  StaffingRequestResponse,
} from '../services/assignmentService';
import { invoiceService, InvoiceResponse } from '../services/invoiceService';
import {
  Users,
  Building2,
  ClipboardList,
  CheckCircle2,
  Clock,
  TrendingUp,
  DollarSign,
  Plus,
  ArrowRight,
  Calendar,
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const { user, activeRole } = useAuthStore();

  // ─── Fetch real data from backend ────────────────────
  const { data: staffingRequests, isLoading: loadingRequests } = useQuery({
    queryKey: ['staffing-requests', 'dashboard'],
    queryFn: () => staffingRequestService.getAll(0, 10),
  });

  const { data: assignments, isLoading: loadingAssignments } = useQuery({
    queryKey: ['assignments', 'dashboard'],
    queryFn: () => assignmentService.getAll(0, 20),
  });

  const { data: invoices, isLoading: loadingInvoices } = useQuery({
    queryKey: ['invoices', 'dashboard'],
    queryFn: () => invoiceService.getAll(0, 10),
  });

  const requests = staffingRequests?.content || [];
  const assignmentList = assignments?.content || [];
  const invoiceList = invoices?.content || [];

  // Role-based filtering
  const ongoingAssignments = assignmentList.filter(
    (a: AssignmentResponse) => a.status === 'ACTIVE'
  );
  const completedAssignments = assignmentList.filter(
    (a: AssignmentResponse) => a.status === 'COMPLETED'
  );
  const pendingAssignments = assignmentList.filter(
    (a: AssignmentResponse) => a.status === 'PENDING' || a.status === 'ACCEPTED'
  );

  const isAdmin = activeRole === 'ROLE_SUPER_ADMIN' || activeRole === 'ROLE_ADMIN';
  const isStaff = activeRole === 'ROLE_STAFF';
  const isClient = activeRole === 'ROLE_USER';

  const isLoading = loadingRequests || loadingAssignments || loadingInvoices;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome back, ${user?.firstName || 'User'}`}
        description={`NurseAdda Workforce Management`}
        badge={
          <Badge variant="primary" dot>
            System Live
          </Badge>
        }
        actions={
          <div className="flex gap-2">
            {(isAdmin || isClient) && (
              <Link to="/requests">
                <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
                  New Staffing Request
                </Button>
              </Link>
            )}
          </div>
        }
      />

      {/* ─── Key Metrics Grid ────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card hoverable>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                {isStaff ? 'My Assignments' : 'Staffing Requests'}
              </p>
              <p className="text-2xl font-black text-slate-900 dark:text-slate-100 mt-1">
                {requests.length}
              </p>
              <div className="flex items-center gap-1 mt-1 text-xs text-emerald-600 dark:text-emerald-400 font-semibold">
                <TrendingUp className="w-3.5 h-3.5" />
                <span>Total</span>
              </div>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-sky-50 dark:bg-sky-950/60 text-sky-600 dark:text-sky-400 flex items-center justify-center shrink-0">
              <ClipboardList className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>

        <Card hoverable>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Active Assignments
              </p>
              <p className="text-2xl font-black text-slate-900 dark:text-slate-100 mt-1">
                {ongoingAssignments.length}
              </p>
              <div className="flex items-center gap-1 mt-1 text-sky-600 dark:text-sky-400 font-semibold text-xs">
                <Calendar className="w-3.5 h-3.5" />
                <span>Ongoing</span>
              </div>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-sky-50 dark:bg-sky-950/60 text-sky-600 dark:text-sky-400 flex items-center justify-center shrink-0">
              <Users className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>

        <Card hoverable>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Completed
              </p>
              <p className="text-2xl font-black text-slate-900 dark:text-slate-100 mt-1">
                {completedAssignments.length}
              </p>
              <div className="flex items-center gap-1 mt-1 text-xs text-emerald-600 dark:text-emerald-400 font-semibold">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>All shifts</span>
              </div>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
              <CheckCircle2 className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>

        <Card hoverable>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Invoices
              </p>
              <p className="text-2xl font-black text-slate-900 dark:text-slate-100 mt-1">
                {invoiceList.length}
              </p>
              <div className="flex items-center gap-1 mt-1 text-xs text-amber-600 dark:text-amber-400 font-semibold">
                <Clock className="w-3.5 h-3.5" />
                <span>Total</span>
              </div>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-amber-50 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
              <DollarSign className="w-6 h-6" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* ─── Main Grid ───────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Staffing Requests */}
        <Card className="lg:col-span-7">
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <ClipboardList className="w-5 h-5 text-sky-600 dark:text-sky-400" />
                <CardTitle>
                  {isStaff ? 'Available Requests' : 'Recent Staffing Demands'}
                </CardTitle>
              </div>
              <Link to="/requests">
                <Button variant="ghost" size="sm" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
                  View All
                </Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent className="p-0 divide-y divide-slate-100 dark:divide-slate-800">
            {requests.length > 0 ? (
              requests.slice(0, 5).map((req: StaffingRequestResponse) => (
                <div
                  key={req.id}
                  className="p-4 flex items-center justify-between hover:bg-slate-50/50 dark:hover:bg-slate-800/40 transition-colors"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sm text-slate-900 dark:text-slate-100">
                        {req.designation}
                      </span>
                      <Badge
                        variant={
                          req.status === 'PENDING'
                            ? 'warning'
                            : req.status === 'APPROVED'
                              ? 'success'
                              : 'info'
                        }
                        size="sm"
                      >
                        {req.status}
                      </Badge>
                    </div>
                    <p className="text-xs text-slate-600 dark:text-slate-400">
                      <span className="font-semibold text-slate-800 dark:text-slate-200">
                        {req.clientName}
                      </span>{' '}
                      &bull; {req.location}
                    </p>
                    <div className="text-[11px] text-slate-400 flex items-center gap-2">
                      <span>{req.shift}</span>
                      <span>&bull;</span>
                      <span>{req.numberOfStaff} staff needed</span>
                    </div>
                  </div>
                  <Link to="/requests">
                    <Button variant="primary" size="sm">
                      {isStaff ? 'Apply' : 'View'}
                    </Button>
                  </Link>
                </div>
              ))
            ) : (
              <EmptyState
                title="No staffing requests"
                description="Create your first staffing request to get started."
              />
            )}
          </CardContent>
        </Card>

        {/* Right Column: Recent Assignments */}
        <Card className="lg:col-span-5">
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <Clock className="w-5 h-5 text-sky-600 dark:text-sky-400" />
                <CardTitle>Recent Assignments</CardTitle>
              </div>
              <Link to="/assignments">
                <Button variant="ghost" size="sm" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
                  View All
                </Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent className="p-4 space-y-3">
            {assignmentList.length > 0 ? (
              assignmentList.slice(0, 5).map((asg: AssignmentResponse) => (
                <Link
                  key={asg.id}
                  to={`/assignments/${asg.id}`}
                  className="flex items-center justify-between p-3 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/60 dark:bg-slate-800/40 hover:bg-slate-100/60 dark:hover:bg-slate-700/40 transition-colors"
                >
                  <div>
                    <p className="text-xs font-bold text-slate-900 dark:text-slate-100">
                      {asg.staffName}
                    </p>
                    <p className="text-[10px] text-slate-400">
                      {asg.designation} &bull; {asg.location}
                    </p>
                  </div>
                  <Badge
                    variant={
                      asg.status === 'ACTIVE'
                        ? 'success'
                        : asg.status === 'COMPLETED'
                          ? 'neutral'
                          : asg.status === 'PENDING'
                            ? 'warning'
                            : 'info'
                    }
                    size="sm"
                  >
                    {asg.status}
                  </Badge>
                </Link>
              ))
            ) : (
              <EmptyState
                title="No assignments yet"
                description="Assignments will appear here once created."
              />
            )}
          </CardContent>
        </Card>
      </div>

      {/* ─── Recent Invoices ─────────────────────────── */}
      {invoiceList.length > 0 && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center gap-2">
                <DollarSign className="w-5 h-5 text-sky-600 dark:text-sky-400" />
                <CardTitle>Recent Invoices</CardTitle>
              </div>
              <Link to="/invoices">
                <Button variant="ghost" size="sm" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
                  View All
                </Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent className="p-0 divide-y divide-slate-100 dark:divide-slate-800">
            {invoiceList.slice(0, 3).map((inv: InvoiceResponse) => (
              <div
                key={inv.id}
                className="p-4 flex items-center justify-between hover:bg-slate-50/50 dark:hover:bg-slate-800/40 transition-colors"
              >
                <div className="space-y-1">
                  <p className="font-bold text-sm text-slate-900 dark:text-slate-100">
                    {inv.staffName}
                  </p>
                  <p className="text-xs text-slate-500">
                    {inv.designation} &bull; {inv.location}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-bold text-sm text-slate-900 dark:text-slate-100">
                    ₹{inv.totalAmount?.toLocaleString()}
                  </p>
                  <Badge
                    variant={
                      inv.status === 'PAID'
                        ? 'success'
                        : inv.status === 'OVERDUE'
                          ? 'danger'
                          : 'info'
                    }
                    size="sm"
                  >
                    {inv.status}
                  </Badge>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}
    </div>
  );
};
