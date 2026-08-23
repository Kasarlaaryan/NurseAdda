import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { Modal } from '../components/common/Modal';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { useToast } from '../components/common/Toast';
import {
  staffingRequestService,
  assignmentService,
  StaffingRequestResponse,
  AssignmentResponse,
} from '../services/assignmentService';
import { StaffProfileResponse } from '../services/authService';
import { staffService } from '../services/staffService';
import {
  Plus,
  Search,
  UserCheck,
  AlertTriangle,
  UserPlus,
  Clock,
  MapPin,
} from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';

/** Map backend status to display-friendly labels */
function statusDisplay(status: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    PENDING: { label: 'Open', variant: 'warning' },
    APPROVED: { label: 'Approved', variant: 'success' },
    REJECTED: { label: 'Rejected', variant: 'danger' },
    ASSIGNED: { label: 'Assigned', variant: 'info' },
    COMPLETED: { label: 'Completed', variant: 'neutral' },
    EXPIRED: { label: 'Expired', variant: 'danger' },
  };
  return map[status] || { label: status, variant: 'info' as const };
}

export const StaffingRequestsPage: React.FC = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { activeRole } = useAuthStore();
  const queryClient = useQueryClient();

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRequest, setSelectedRequest] = useState<StaffingRequestResponse | null>(null);

  const isUserRole = activeRole === 'ROLE_USER';
  const isAdmin = activeRole === 'ROLE_SUPER_ADMIN' || activeRole === 'ROLE_ADMIN';

  // ─── Fetch staffing requests ─────────────────────────
  const { data: requestsData, isLoading } = useQuery({
    queryKey: ['staffing-requests'],
    queryFn: () => staffingRequestService.getAll(0, 50),
  });

  // ─── Fetch staff profiles for matching modal ─────────
  const { data: staffData } = useQuery({
    queryKey: ['staff-profiles'],
    queryFn: () => staffService.getAllStaff(0, 100),
    enabled: Boolean(selectedRequest),
  });

  // ─── Fetch assignments for selected request ──────────
  const { data: requestAssignments } = useQuery({
    queryKey: ['request-assignments', selectedRequest?.id],
    queryFn: () => assignmentService.getRequestAssignments(selectedRequest!.id),
    enabled: Boolean(selectedRequest),
  });

  // ─── Accept request mutation (staff) ─────────────────
  const acceptMutation = useMutation({
    mutationFn: (id: number) => staffingRequestService.accept(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staffing-requests'] });
      queryClient.invalidateQueries({ queryKey: ['assignments'] });
      showToast('success', 'Accepted', 'You have accepted this staffing request.');
    },
    onError: () => {
      showToast('error', 'Failed', 'Could not accept request.');
    },
  });

  // ─── Cancel request mutation (client) ────────────────
  const cancelMutation = useMutation({
    mutationFn: (id: number) => staffingRequestService.cancel(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['staffing-requests'] });
      const refundMsg = data.refunded
        ? ` ${data.refundPercentage}% refund (₹${data.refundAmount}) processed via Razorpay.`
        : ' No advance payment to refund.';
      showToast('success', 'Request Cancelled', `Staffing request has been cancelled.${refundMsg}`);
      setSelectedRequest(null);
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Failed', err?.response?.data?.message || 'Could not cancel request.');
    },
  });

  const requests = requestsData?.content || [];
  const staffProfiles: StaffProfileResponse[] = (staffData as any)?.content || staffData || [];
  const assignments: AssignmentResponse[] = requestAssignments || [];

  const filteredRequests = requests.filter((r: StaffingRequestResponse) => {
    const q = searchQuery.toLowerCase();
    return (
      r.designation?.toLowerCase().includes(q) ||
      r.clientName?.toLowerCase().includes(q) ||
      r.location?.toLowerCase().includes(q)
    );
  });

  const handleAcceptRequest = (requestId: number) => {
    acceptMutation.mutate(requestId);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Staffing Shift Requests"
        description={
          isUserRole
            ? "Manage your facility's staffing demands."
            : 'Broadcast and match urgent shift orders with verified healthcare staff.'
        }
        actions={
          (isUserRole || isAdmin) && (
            <Button
              variant="primary"
              size="sm"
              leftIcon={<Plus className="w-4 h-4" />}
              onClick={() => navigate('/requests/create')}
            >
              New Staffing Request
            </Button>
          )
        }
      />

      {/* ─── Search ──────────────────────────────────── */}
      <div className="w-full md:w-80">
        <Input
          placeholder="Search by designation, client, or location..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          leftIcon={<Search className="w-4 h-4 text-slate-400" />}
        />
      </div>

      {/* ─── Request Cards ───────────────────────────── */}
      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Loader />
        </div>
      ) : filteredRequests.length === 0 ? (
        <EmptyState
          title="No staffing requests"
          description="Create your first staffing request to get started."
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredRequests.map((req: StaffingRequestResponse) => {
            const { label: statusLabel, variant } = statusDisplay(req.status);
            return (
              <Card key={req.id} hoverable className="flex flex-col justify-between">
                <CardHeader className="py-4">
                  <div className="flex items-center justify-between w-full">
                    <span className="font-extrabold text-sm text-slate-900 dark:text-slate-100">
                      {req.designation}
                    </span>
                    <Badge variant={variant} size="sm">
                      {statusLabel}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="py-3 space-y-3">
                  <div>
                    <h4 className="font-bold text-sm text-slate-900 dark:text-slate-100">
                      {req.clientName}
                    </h4>
                    <div className="flex items-center gap-1 mt-0.5">
                      <MapPin className="w-3 h-3 text-slate-400" />
                      <p className="text-xs text-slate-500">{req.location}</p>
                    </div>
                    {req.locationLink && (
                      <a
                        href={req.locationLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-500 hover:text-amber-600 hover:underline mt-1"
                      >
                        <MapPin className="w-3 h-3" />
                        Open on Google Maps
                      </a>
                    )}
                  </div>

                  <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 space-y-1.5 text-xs">
                    <div className="flex justify-between">
                      <span className="text-slate-500">Shift:</span>
                      <span className="font-medium">{req.shift}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500">Staff Needed:</span>
                      <span className="font-semibold text-slate-900 dark:text-slate-100">
                        {req.numberOfStaff}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500">Dates:</span>
                      <span className="font-medium">
                        {req.startDate} → {req.endDate}
                      </span>
                    </div>
                    {req.startTime && req.endTime && (
                      <div className="flex justify-between">
                        <span className="text-slate-500">Time:</span>
                        <span className="font-medium">
                          {req.startTime} → {req.endTime}
                        </span>
                      </div>
                    )}
                    {req.estimatedTotal > 0 && (
                      <div className="flex justify-between">
                        <span className="text-slate-500">Est. Total:</span>
                        <span className="font-bold text-emerald-600">
                          ₹{req.estimatedTotal.toLocaleString()}
                        </span>
                      </div>
                    )}
                    {req.hourlyBillingRate > 0 && (
                      <div className="flex justify-between">
                        <span className="text-slate-500">Billing Rate:</span>
                        <span className="font-medium">₹{req.hourlyBillingRate}/hr</span>
                      </div>
                    )}
                    {req.hourlyPayRate > 0 && (
                      <div className="flex justify-between">
                        <span className="text-slate-500">Pay Rate:</span>
                        <span className="font-medium">₹{req.hourlyPayRate}/hr</span>
                      </div>
                    )}
                    {req.advanceAmount > 0 && (
                      <div className="flex justify-between">
                        <span className="text-slate-500">Advance (40%):</span>
                        <span className="font-bold text-amber-600">
                          ₹{req.advanceAmount.toLocaleString()}
                        </span>
                      </div>
                    )}
                  </div>

                  {req.requiredSkills && (
                    <div className="flex flex-wrap gap-1">
                      {req.requiredSkills.split(',').map((skill: string) => (
                        <span
                          key={skill.trim()}
                          className="px-2 py-0.5 text-[10px] bg-slate-100 dark:bg-slate-800 rounded-full text-slate-500"
                        >
                          {skill.trim()}
                        </span>
                      ))}
                    </div>
                  )}

                  <div className="flex items-center justify-between text-xs">
                    <Badge variant={variant} size="sm" dot>
                      {statusLabel}
                    </Badge>
                    <span className="text-[11px] text-slate-400 flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {req.createdAt
                        ? new Date(req.createdAt).toLocaleDateString()
                        : ''}
                    </span>
                  </div>
                </CardContent>
                <div className="p-4 bg-slate-50/50 dark:bg-slate-900/50 border-t border-slate-100 dark:border-slate-800 rounded-b-2xl">
                  {isUserRole ? (
                    <Button
                      variant="outline"
                      size="sm"
                      className="w-full"
                      leftIcon={<UserCheck className="w-3.5 h-3.5" />}
                      onClick={() => setSelectedRequest(req)}
                    >
                      View Details
                    </Button>
                  ) : (
                    <Button
                      variant="primary"
                      size="sm"
                      className="w-full"
                      leftIcon={<UserCheck className="w-3.5 h-3.5" />}
                      onClick={() => setSelectedRequest(req)}
                      disabled={req.status === 'COMPLETED' || req.status === 'REJECTED'}
                    >
                      {req.status === 'PENDING' ? 'Manage Request' : 'View Assignments'}
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* ─── Detail / Assign Modal ───────────────────── */}
      {selectedRequest && (
        <Modal
          isOpen={true}
          onClose={() => setSelectedRequest(null)}
          title={`${selectedRequest.designation} — ${selectedRequest.clientName}`}
          maxWidth="md"
        >
          <div className="space-y-4">
            {/* Request Info */}
            <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 space-y-2 text-xs">
              <div className="flex justify-between">
                <span className="text-slate-500">Location:</span>
                <div>
                  <span className="font-medium">{selectedRequest.location}</span>
                  {selectedRequest.locationLink && (
                    <a
                      href={selectedRequest.locationLink}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block text-[10px] font-bold text-amber-500 hover:text-amber-600 hover:underline mt-0.5"
                    >
                      📍 Open on Google Maps
                    </a>
                  )}
                </div>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Shift:</span>
                <span className="font-medium">{selectedRequest.shift}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Dates:</span>
                <span className="font-medium">
                  {selectedRequest.startDate} → {selectedRequest.endDate}
                </span>
              </div>
              {selectedRequest.startTime && selectedRequest.endTime && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Time:</span>
                  <span className="font-medium">
                    {selectedRequest.startTime} → {selectedRequest.endTime}
                  </span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-slate-500">Staff Needed:</span>
                <span className="font-semibold">{selectedRequest.numberOfStaff}</span>
              </div>
              {selectedRequest.hourlyBillingRate > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Billing Rate:</span>
                  <span className="font-medium">₹{selectedRequest.hourlyBillingRate}/hr</span>
                </div>
              )}
              {selectedRequest.hourlyPayRate > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Pay Rate:</span>
                  <span className="font-medium">₹{selectedRequest.hourlyPayRate}/hr</span>
                </div>
              )}
              {selectedRequest.estimatedTotal > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Est. Total:</span>
                  <span className="font-bold text-emerald-600">₹{selectedRequest.estimatedTotal.toLocaleString()}</span>
                </div>
              )}
              {selectedRequest.advanceAmount > 0 && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Advance (40%):</span>
                  <span className="font-bold text-amber-600">₹{selectedRequest.advanceAmount.toLocaleString()}</span>
                </div>
              )}
            </div>

            {/* Admin actions */}
            {isAdmin && selectedRequest.status === 'PENDING' && (
              <div className="flex gap-2">
                <Button
                  variant="primary"
                  size="sm"
                  className="flex-1"
                  onClick={() => {
                    staffingRequestService
                      .updateStatus(selectedRequest.id, 'APPROVED')
                      .then(() => {
                        queryClient.invalidateQueries({ queryKey: ['staffing-requests'] });
                        setSelectedRequest(null);
                        showToast('success', 'Approved', 'Request has been approved.');
                      });
                  }}
                >
                  Approve
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="flex-1 text-red-500"
                  onClick={() => {
                    staffingRequestService
                      .updateStatus(selectedRequest.id, 'REJECTED')
                      .then(() => {
                        queryClient.invalidateQueries({ queryKey: ['staffing-requests'] });
                        setSelectedRequest(null);
                        showToast('success', 'Rejected', 'Request has been rejected.');
                      });
                  }}
                >
                  Reject
                </Button>
              </div>
            )}

            {/* Client cancel */}
            {isUserRole && (selectedRequest.status === 'PENDING' || selectedRequest.status === 'APPROVED') && (
              <div className="space-y-2">
                <div className="p-3 rounded-xl bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-800/30 text-[11px] text-red-700 dark:text-red-400">
                  <p className="font-bold mb-1">Cancellation Policy:</p>
                  <ul className="list-disc list-inside space-y-0.5">
                    <li>Cancel <strong>30+ min before</strong> shift: <strong>80% refund</strong>, 20% penalty</li>
                    <li>Cancel <strong>within 30 min</strong> of shift: <strong>20% refund</strong>, 80% penalty</li>
                    <li>Cancel <strong>after shift starts</strong>: No refund</li>
                  </ul>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 border border-red-200 dark:border-red-800/30"
                  onClick={() => {
                    if (window.confirm('Are you sure you want to cancel this request? A refund will be processed based on the cancellation policy.')) {
                      cancelMutation.mutate(selectedRequest.id);
                    }
                  }}
                  isLoading={cancelMutation.isPending}
                >
                  Cancel Request
                </Button>
              </div>
            )}

            {/* Refund info */}
            {selectedRequest.refunded && (
              <div className="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-800/30 space-y-1 text-xs">
                <div className="flex items-center gap-2 text-emerald-700 dark:text-emerald-400">
                  <span className="font-bold">Refund Processed</span>
                </div>
                <div className="flex justify-between text-emerald-600 dark:text-emerald-300">
                  <span>Refund Percentage:</span>
                  <span className="font-bold">{selectedRequest.refundPercentage}%</span>
                </div>
                <div className="flex justify-between text-emerald-600 dark:text-emerald-300">
                  <span>Refund Amount:</span>
                  <span className="font-bold">₹{selectedRequest.refundAmount?.toLocaleString()}</span>
                </div>
                {selectedRequest.razorpayRefundId && (
                  <div className="flex justify-between text-emerald-600 dark:text-emerald-300">
                    <span>Refund ID:</span>
                    <span className="font-mono text-[10px]">{selectedRequest.razorpayRefundId}</span>
                  </div>
                )}
              </div>
            )}

            {/* Staff accept */}
            {activeRole === 'ROLE_STAFF' && selectedRequest.status === 'PENDING' && (
              <Button
                variant="primary"
                size="sm"
                className="w-full"
                leftIcon={<UserPlus className="w-3.5 h-3.5" />}
                onClick={() => handleAcceptRequest(selectedRequest.id)}
                isLoading={acceptMutation.isPending}
              >
                Accept This Request
              </Button>
            )}

            {/* Assignments list */}
            {assignments.length > 0 && (
              <div className="space-y-2">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Assigned Staff ({assignments.length})
                </h4>
                {assignments.map((asg: AssignmentResponse) => (
                  <div
                    key={asg.id}
                    className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 flex items-center justify-between"
                  >
                    <div>
                      <p className="text-xs font-bold text-slate-900 dark:text-slate-100">
                        {asg.staffName}
                      </p>
                      <p className="text-[10px] text-slate-400">
                        {asg.staffCategory} &bull; {asg.location}
                      </p>
                    </div>
                    <Badge
                      variant={
                        asg.status === 'ACTIVE'
                          ? 'success'
                          : asg.status === 'COMPLETED'
                            ? 'neutral'
                            : 'info'
                      }
                      size="sm"
                    >
                      {asg.status}
                    </Badge>
                  </div>
                ))}
              </div>
            )}

            {/* Staff pool for admin */}
            {isAdmin && selectedRequest.status !== 'COMPLETED' && (
              <div className="space-y-2">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Available Staff ({staffProfiles.length})
                </h4>
                <div className="space-y-2 max-h-[300px] overflow-y-auto">
                  {staffProfiles.length > 0 ? (
                    staffProfiles.map((staff: StaffProfileResponse) => {
                      const isAssigned = assignments.some(
                        (a: AssignmentResponse) => a.staffProfileId === staff.id
                      );
                      return (
                        <div
                          key={staff.id}
                          className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 flex items-center justify-between"
                        >
                          <div>
                            <p className="text-xs font-bold text-slate-900 dark:text-slate-100">
                              {staff.firstName} {staff.lastName}
                            </p>
                            <p className="text-[10px] text-slate-400">
                              {staff.staffCategory} &bull; {staff.email}
                            </p>
                          </div>
                          {isAssigned ? (
                            <Badge variant="success" size="sm">
                              Assigned
                            </Badge>
                          ) : (
                            <Badge variant="info" size="sm">
                              {staff.verified ? 'Verified' : 'Pending'}
                            </Badge>
                          )}
                        </div>
                      );
                    })
                  ) : (
                    <div className="py-8 text-center">
                      <AlertTriangle className="w-8 h-8 text-amber-500 mx-auto mb-2 opacity-50" />
                      <p className="text-xs text-slate-500">No staff profiles found.</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </Modal>
      )}

    </div>
  );
};
