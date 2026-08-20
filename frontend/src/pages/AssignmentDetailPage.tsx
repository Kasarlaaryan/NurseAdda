import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Modal } from '../components/common/Modal';
import { Input } from '../components/common/Input';
import { Loader } from '../components/common/Loader';
import { useAuthStore } from '../store/useAuthStore';
import { useToast } from '../components/common/Toast';
import { assignmentService, AssignmentResponse } from '../services/assignmentService';
import {
  ArrowLeft,
  Building2,
  User,
  Calendar,
  Clock,
  MapPin,
  FileText,
  ShieldCheck,
  ClipboardCheck,
  KeyRound,
  ShieldAlert,
} from 'lucide-react';

/** Map backend status to display-friendly values */
function statusDisplay(status: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    PENDING: { label: 'Pending', variant: 'warning' },
    ACCEPTED: { label: 'Accepted', variant: 'info' },
    REJECTED: { label: 'Rejected', variant: 'danger' },
    ACTIVE: { label: 'In Progress', variant: 'success' },
    COMPLETED: { label: 'Completed', variant: 'neutral' },
    CANCELLED: { label: 'Cancelled', variant: 'danger' },
  };
  return map[status] || { label: status, variant: 'info' as const };
}

export const AssignmentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { activeRole } = useAuthStore();
  const { showToast } = useToast();
  const queryClient = useQueryClient();

  const [showStatusModal, setShowStatusModal] = useState(false);
  const [targetStatus, setTargetStatus] = useState('');
  const [statusNotes, setStatusNotes] = useState('');

  const isStaff = activeRole === 'ROLE_STAFF';

  // ─── Fetch assignment by ID ──────────────────────────
  const { data: assignment, isLoading } = useQuery({
    queryKey: ['assignment', id],
    queryFn: () => assignmentService.getById(Number(id)),
    enabled: Boolean(id),
  });

  // ─── Update status mutation ──────────────────────────
  const statusMutation = useMutation({
    mutationFn: ({ status, notes }: { status: string; notes?: string }) =>
      assignmentService.updateStatus(Number(id), status, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['assignment', id] });
      queryClient.invalidateQueries({ queryKey: ['assignments'] });
      setShowStatusModal(false);
      setStatusNotes('');
      showToast('success', 'Status Updated', `Assignment status changed to ${targetStatus}.`);
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Failed', err?.response?.data?.message || 'Could not update status.');
    },
  });

  const handleStatusChange = () => {
    statusMutation.mutate({ status: targetStatus, notes: statusNotes || undefined });
  };

  const initiateStatusChange = (status: string) => {
    setTargetStatus(status);
    setShowStatusModal(true);
    setStatusNotes('');
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader />
      </div>
    );
  }

  if (!assignment) {
    return (
      <div className="flex flex-col items-center justify-center py-20 space-y-4">
        <p className="text-slate-500">Assignment not found.</p>
        <Button
          variant="ghost"
          onClick={() => navigate('/assignments')}
          leftIcon={<ArrowLeft className="w-4 h-4" />}
        >
          Back to Assignments
        </Button>
      </div>
    );
  }

  const { label: statusLabel, variant } = statusDisplay(assignment.status);

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" onClick={() => navigate('/assignments')} className="p-2">
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <PageHeader
          title={`Assignment #${assignment.id}`}
          description="Detailed view of shift deployment, contacts, and status."
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* ─── Main Details ──────────────────────────── */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="border-b border-slate-100 dark:border-slate-800">
              <div className="flex items-center justify-between w-full">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-xl bg-sky-500/10 text-sky-500">
                    <ClipboardCheck className="w-6 h-6" />
                  </div>
                  <CardTitle>Assignment Details</CardTitle>
                </div>
                <Badge variant={variant}>{statusLabel}</Badge>
              </div>
            </CardHeader>
            <CardContent className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="space-y-4">
                  <div className="flex items-start gap-4">
                    <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-500">
                      <Building2 className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">
                        Designation
                      </p>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {assignment.designation}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start gap-4">
                    <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-500">
                      <MapPin className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">
                        Location
                      </p>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {assignment.location}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="space-y-4">
                  <div className="flex items-start gap-4">
                    <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-500">
                      <Clock className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">
                        Shift
                      </p>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {assignment.shift}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start gap-4">
                    <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-500">
                      <User className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-[10px] font-black uppercase tracking-widest text-slate-500">
                        Assigned By
                      </p>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {assignment.assignedByName || '—'}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              {assignment.notes && (
                <div className="mt-8 p-4 rounded-2xl bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-800">
                  <div className="flex items-center gap-2 mb-3">
                    <FileText className="w-4 h-4 text-sky-500" />
                    <h4 className="text-xs font-black uppercase tracking-widest text-slate-500">
                      Notes
                    </h4>
                  </div>
                  <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                    {assignment.notes}
                  </p>
                </div>
              )}

              <div className="mt-6 grid grid-cols-2 gap-4 text-xs text-slate-500">
                {assignment.sentToClientAt && (
                  <div>
                    <span className="font-bold">Sent to Client:</span>{' '}
                    {new Date(assignment.sentToClientAt).toLocaleString()}
                  </div>
                )}
                {assignment.acceptedAt && (
                  <div>
                    <span className="font-bold">Accepted:</span>{' '}
                    {new Date(assignment.acceptedAt).toLocaleString()}
                  </div>
                )}
                {assignment.completedAt && (
                  <div>
                    <span className="font-bold">Completed:</span>{' '}
                    {new Date(assignment.completedAt).toLocaleString()}
                  </div>
                )}
                {assignment.createdAt && (
                  <div>
                    <span className="font-bold">Created:</span>{' '}
                    {new Date(assignment.createdAt).toLocaleString()}
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* ─── Compliance ───────────────────────────── */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-emerald-500" />
                Compliance
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {[
                  {
                    label: 'Credential Verification',
                    status: assignment.staffCategory ? 'Verified' : 'Pending',
                    icon: ShieldCheck,
                  },
                  {
                    label: 'Client Approval',
                    status: assignment.sentToClient ? 'Sent' : 'Pending',
                    icon: ClipboardCheck,
                  },
                ].map((item, idx) => (
                  <div
                    key={idx}
                    className="flex items-center justify-between p-3 rounded-xl border border-slate-100 dark:border-slate-800"
                  >
                    <div className="flex items-center gap-3">
                      <item.icon className="w-4 h-4 text-slate-400" />
                      <span className="text-xs font-bold text-slate-700 dark:text-slate-300">
                        {item.label}
                      </span>
                    </div>
                    <Badge variant={item.status === 'Verified' || item.status === 'Sent' ? 'success' : 'warning'} size="sm">
                      {item.status}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* ─── Sidebar ───────────────────────────────── */}
        <div className="space-y-6">
          {/* Assigned Staff */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Assigned Staff</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center gap-4 p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
                  <div className="w-12 h-12 rounded-2xl bg-slate-200 dark:bg-slate-800 flex items-center justify-center text-slate-400">
                    <User className="w-6 h-6" />
                  </div>
                  <div>
                    <p className="text-sm font-black text-slate-900 dark:text-white">
                      {assignment.staffName}
                    </p>
                    <p className="text-[10px] text-slate-500 font-bold uppercase">
                      {assignment.staffCategory}
                    </p>
                    <p className="text-[10px] text-slate-400">{assignment.staffEmail}</p>
                  </div>
                </div>

                <Button
                  variant="secondary"
                  size="sm"
                  className="w-full"
                  onClick={() => navigate(`/staff/${assignment.staffProfileId}`)}
                >
                  View Staff Profile
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Status Actions */}
          <div className="flex flex-col gap-3">
            {isStaff && (
              <>
                {assignment.status === 'PENDING' && (
                  <Button
                    variant="primary"
                    className="w-full h-12 font-black"
                    onClick={() => initiateStatusChange('ACTIVE')}
                    leftIcon={<Clock className="w-4 h-4" />}
                  >
                    Start Assignment
                  </Button>
                )}
                {assignment.status === 'ACTIVE' && (
                  <Button
                    variant="danger"
                    className="w-full h-12 font-black"
                    onClick={() => initiateStatusChange('COMPLETED')}
                    leftIcon={<ShieldAlert className="w-4 h-4" />}
                  >
                    End Assignment
                  </Button>
                )}
                {assignment.status === 'COMPLETED' && (
                  <Button
                    variant="secondary"
                    className="w-full h-12 font-black opacity-50 cursor-not-allowed"
                    disabled
                    leftIcon={<ClipboardCheck className="w-4 h-4" />}
                  >
                    Assignment Completed
                  </Button>
                )}
              </>
            )}

            {!isStaff && assignment.status === 'PENDING' && (
              <Button
                variant="primary"
                className="w-full h-12 font-black"
                onClick={() => initiateStatusChange('ACCEPTED')}
                leftIcon={<ClipboardCheck className="w-4 h-4" />}
              >
                Approve Assignment
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* ─── Status Change Modal ─────────────────────── */}
      <Modal
        isOpen={showStatusModal}
        onClose={() => setShowStatusModal(false)}
        title={`Change Status to ${targetStatus}`}
        maxWidth="sm"
      >
        <div className="space-y-4 py-2">
          <div className="flex flex-col items-center text-center space-y-3">
            <div className="w-16 h-16 rounded-full bg-sky-100 dark:bg-sky-900/30 flex items-center justify-center text-sky-600">
              <KeyRound className="w-8 h-8" />
            </div>
            <div>
              <p className="text-sm font-bold text-slate-900 dark:text-white">
                Confirm Status Change
              </p>
              <p className="text-xs text-slate-500 mt-1">
                You are about to change this assignment to{' '}
                <span className="font-bold text-sky-500">{targetStatus}</span>.
              </p>
            </div>
          </div>

          <Input
            label="Notes (optional)"
            placeholder="Add any notes about this status change..."
            value={statusNotes}
            onChange={(e) => setStatusNotes(e.target.value)}
          />

          <div className="flex flex-col gap-2">
            <Button
              variant="primary"
              className="w-full"
              onClick={handleStatusChange}
              isLoading={statusMutation.isPending}
            >
              Confirm
            </Button>
            <Button variant="ghost" className="w-full" onClick={() => setShowStatusModal(false)}>
              Cancel
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
