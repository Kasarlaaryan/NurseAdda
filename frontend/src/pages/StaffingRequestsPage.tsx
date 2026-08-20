import React, { useState } from 'react';
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
  FileText,
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
  const { showToast } = useToast();
  const { activeRole } = useAuthStore();
  const queryClient = useQueryClient();

  const [searchQuery, setSearchQuery] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
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

  // ─── Create staffing request mutation ────────────────
  const createMutation = useMutation({
    mutationFn: staffingRequestService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staffing-requests'] });
      setIsModalOpen(false);
      showToast('success', 'Request Created', 'Staffing request has been broadcast to available staff.');
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Failed', err?.response?.data?.message || 'Could not create request.');
    },
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

  // ─── New Request Form State ──────────────────────────
  const [formDesignation, setFormDesignation] = useState('Registered Nurse (RN)');
  const [formLocation, setFormLocation] = useState('');
  const [formRequestType, setFormRequestType] = useState<'ON_CALL' | 'MONTHLY'>('ON_CALL');
  const [formShift, setFormShift] = useState('Night Shift');
  const [formStartDate, setFormStartDate] = useState('');
  const [formEndDate, setFormEndDate] = useState('');
  const [formNumberOfStaff, setFormNumberOfStaff] = useState(1);
  const [formRequiredSkills, setFormRequiredSkills] = useState('');

  const handleCreateRequest = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({
      designation: formDesignation,
      location: formLocation,
      requestType: formRequestType,
      shift: formShift,
      startDate: formStartDate,
      endDate: formEndDate,
      numberOfStaff: formNumberOfStaff,
      requiredSkills: formRequiredSkills,
    });
  };

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
              onClick={() => setIsModalOpen(true)}
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
                    {req.estimatedTotal > 0 && (
                      <div className="flex justify-between">
                        <span className="text-slate-500">Est. Total:</span>
                        <span className="font-bold text-emerald-600">
                          ₹{req.estimatedTotal.toLocaleString()}
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
                <span className="font-medium">{selectedRequest.location}</span>
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
              <div className="flex justify-between">
                <span className="text-slate-500">Staff Needed:</span>
                <span className="font-semibold">{selectedRequest.numberOfStaff}</span>
              </div>
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

      {/* ─── Create Request Modal ────────────────────── */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="New Staffing Request"
        maxWidth="lg"
      >
        <form onSubmit={handleCreateRequest} className="space-y-4">
          <div>
            <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-1">
              Designation / Role
            </label>
            <select
              className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5"
              value={formDesignation}
              onChange={(e) => setFormDesignation(e.target.value)}
              required
            >
              {[
                'Registered Nurse (RN)',
                'Nurse Practitioner (NP)',
                'Licensed Practical Nurse (LPN)',
                'General Nursing and Midwifery (GNM)',
                'Auxiliary Nurse Midwifery (ANM)',
                'Physiotherapist',
                'Medical Lab Technician',
              ].map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          <Input
            label="Location"
            value={formLocation}
            onChange={(e) => setFormLocation(e.target.value)}
            placeholder="e.g. City Hospital - ICU Wing"
            required
          />

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-1">
                Request Type
              </label>
              <select
                className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5"
                value={formRequestType}
                onChange={(e) => setFormRequestType(e.target.value as 'ON_CALL' | 'MONTHLY')}
              >
                <option value="ON_CALL">On-Call</option>
                <option value="MONTHLY">Monthly</option>
              </select>
            </div>
            <div>
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-1">
                Shift
              </label>
              <select
                className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5"
                value={formShift}
                onChange={(e) => setFormShift(e.target.value)}
              >
                <option value="Day Shift">Day Shift</option>
                <option value="Night Shift">Night Shift</option>
                <option value="24hr On-Call">24hr On-Call</option>
                <option value="Rotational">Rotational</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Start Date"
              type="date"
              value={formStartDate}
              onChange={(e) => setFormStartDate(e.target.value)}
              required
            />
            <Input
              label="End Date"
              type="date"
              value={formEndDate}
              onChange={(e) => setFormEndDate(e.target.value)}
              required
            />
          </div>

          <Input
            label="Number of Staff Needed"
            type="number"
            min={1}
            max={50}
            value={formNumberOfStaff}
            onChange={(e) => setFormNumberOfStaff(Number(e.target.value))}
            required
          />

          <Input
            label="Required Skills (comma-separated)"
            value={formRequiredSkills}
            onChange={(e) => setFormRequiredSkills(e.target.value)}
            placeholder="e.g. Critical Care, ICU, BLS Certification"
          />

          <div className="pt-3 flex justify-end gap-2">
            <Button variant="ghost" size="sm" type="button" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              type="submit"
              isLoading={createMutation.isPending}
            >
              Create Request
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
