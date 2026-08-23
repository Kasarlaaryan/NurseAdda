import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/useAuthStore';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { assignmentService, AssignmentResponse } from '../services/assignmentService';
import { Eye, MapPin, Clock } from 'lucide-react';

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

export const AssignmentsPage: React.FC = () => {
  const { activeRole } = useAuthStore();

  const { data, isLoading } = useQuery({
    queryKey: ['assignments'],
    queryFn: () => assignmentService.getAll(0, 50),
  });

  const assignments = data?.content || [];

  const pageTitle =
    activeRole === 'ROLE_STAFF'
      ? 'My Assignments'
      : activeRole === 'ROLE_USER'
        ? 'My Staff Assignments'
        : 'Assignment & Shift Management';

  const pageDescription =
    activeRole === 'ROLE_STAFF'
      ? 'Track your active shift deployments, hospital contacts, and pay rates.'
      : activeRole === 'ROLE_USER'
        ? 'Monitor healthcare professionals assigned to your facility.'
        : 'Track staff deployments, shift schedules, and assignment statuses.';

  const showStaffColumn = activeRole !== 'ROLE_STAFF';

  return (
    <div className="space-y-6">
      <PageHeader title={pageTitle} description={pageDescription} />

      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Loader />
        </div>
      ) : assignments.length === 0 ? (
        <EmptyState
          title="No assignments found"
          description="Assignments will appear here once staffing requests are accepted."
        />
      ) : (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                {showStaffColumn && <TableHead>Assigned Staff</TableHead>}
                <TableHead>Designation</TableHead>
                <TableHead>Location</TableHead>
                <TableHead>Shift</TableHead>
                <TableHead>Assigned By</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Created</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {assignments.map((asg: AssignmentResponse) => {
                const { label: statusLabel, variant } = statusDisplay(asg.status);
                return (
                  <TableRow key={asg.id}>
                    {showStaffColumn && (
                      <TableCell>
                        <p className="font-bold text-slate-900 dark:text-slate-100">
                          {asg.staffName}
                        </p>
                        <p className="text-xs text-slate-500">{asg.staffEmail}</p>
                      </TableCell>
                    )}
                    <TableCell>
                      <span className="font-semibold text-sm text-slate-800 dark:text-slate-200">
                        {asg.designation}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <MapPin className="w-3 h-3 text-slate-400" />
                        <span className="text-xs">{asg.location}</span>
                      </div>
                      {asg.locationLink && (
                        <a
                          href={asg.locationLink}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-[10px] font-bold text-amber-500 hover:text-amber-600 hover:underline"
                        >
                          📍 Map
                        </a>
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <Clock className="w-3 h-3 text-slate-400" />
                        <span className="text-xs font-medium">{asg.shift}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <p className="text-xs font-medium text-slate-700 dark:text-slate-300">
                        {asg.assignedByName || '—'}
                      </p>
                    </TableCell>
                    <TableCell>
                      <Badge variant={variant} size="sm" dot>
                        {statusLabel}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <span className="text-[11px] text-slate-400">
                        {asg.createdAt
                          ? new Date(asg.createdAt).toLocaleDateString()
                          : '—'}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">
                      <Link to={`/assignments/${asg.id}`}>
                        <Button variant="ghost" size="sm" leftIcon={<Eye className="w-3.5 h-3.5" />}>
                          Details
                        </Button>
                      </Link>
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
