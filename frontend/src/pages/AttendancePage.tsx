import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../store/useAuthStore';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { useToast } from '../components/common/Toast';
import { attendanceService, AttendanceResponse } from '../services/attendanceService';
import { assignmentService } from '../services/assignmentService';
import { Clock, ShieldCheck, Play, Square } from 'lucide-react';

/** Map backend status to display values */
function attendanceStatusDisplay(status: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    CHECKED_IN: { label: 'Checked In', variant: 'success' },
    CHECKED_OUT: { label: 'Checked Out', variant: 'neutral' },
    LATE: { label: 'Late', variant: 'warning' },
    ABSENT: { label: 'Absent', variant: 'danger' },
    OVERTIME: { label: 'Overtime', variant: 'info' },
  };
  return map[status] || { label: status, variant: 'info' as const };
}

export const AttendancePage: React.FC = () => {
  const { user, activeRole } = useAuthStore();
  const { showToast } = useToast();
  const queryClient = useQueryClient();

  const isStaff = activeRole === 'ROLE_STAFF';

  // Fetch today's attendance
  const { data: todayAttendance, isLoading: loadingToday } = useQuery({
    queryKey: ['attendance-today'],
    queryFn: () => attendanceService.getToday(),
    enabled: isStaff,
    retry: false,
  });

  // Fetch attendance history
  const { data: attendanceList, isLoading: loadingHistory } = useQuery({
    queryKey: ['attendance-my'],
    queryFn: () => attendanceService.getMyAttendance(),
    enabled: isStaff,
  });

  // Fetch active assignments for check-in
  const { data: assignmentsData } = useQuery({
    queryKey: ['assignments-active'],
    queryFn: () => assignmentService.getAll(0, 50),
    enabled: isStaff,
  });

  const isCheckedIn = todayAttendance && !todayAttendance.checkOutTime;

  // Check-in mutation
  const checkInMutation = useMutation({
    mutationFn: (data: { assignmentId: number; location?: string }) =>
      attendanceService.checkIn(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance-today'] });
      queryClient.invalidateQueries({ queryKey: ['attendance-my'] });
      showToast('success', 'Checked In', 'Your shift has started. GPS location recorded.');
    },
    onError: () => {
      showToast('error', 'Check-In Failed', 'Could not record your check-in. Please try again.');
    },
  });

  // Check-out mutation
  const checkOutMutation = useMutation({
    mutationFn: (attendanceId: number) => attendanceService.checkOut(attendanceId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance-today'] });
      queryClient.invalidateQueries({ queryKey: ['attendance-my'] });
      showToast('info', 'Checked Out', 'Your shift has ended. Attendance recorded.');
    },
    onError: () => {
      showToast('error', 'Check-Out Failed', 'Could not record your check-out. Please try again.');
    },
  });

  const handleCheckIn = () => {
    // Use first active assignment for check-in
    const activeAssignments = assignmentsData?.content?.filter(
      (a: any) => a.status === 'ACTIVE' || a.status === 'ACCEPTED'
    );
    if (!activeAssignments || activeAssignments.length === 0) {
      showToast('error', 'No Active Assignment', 'You need an active assignment to check in.');
      return;
    }
    checkInMutation.mutate({ assignmentId: activeAssignments[0].id });
  };

  const handleCheckOut = () => {
    if (todayAttendance) {
      checkOutMutation.mutate(todayAttendance.id);
    }
  };

  const isLoading = loadingToday || loadingHistory;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-40">
        <Loader />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title={isStaff ? 'My Shift Attendance' : 'Attendance & Timesheet Tracking'}
        description={isStaff 
          ? 'Track your shift clock-ins, GPS verifications, and timesheet approvals.'
          : 'Real-time shift check-in, GPS geofenced verification, overtime calculations, and supervisor approval logs.'
        }
      />

      {/* Check-In Control Box for Staff */}
      {isStaff && (
        <Card className="bg-gradient-to-r from-sky-900 to-slate-900 text-white border-sky-800 overflow-hidden relative">
          <CardContent className="p-6">
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <ShieldCheck className="w-5 h-5 text-sky-400" />
                  <h3 className="font-bold text-base">GPS Geofenced Digital Clock-In</h3>
                </div>
                <p className="text-xs text-slate-300">
                  {isCheckedIn ? (
                    <>Checked in at <span className="font-bold text-sky-300">{todayAttendance?.checkInTime}</span></>
                  ) : (
                    <>Click below to start your shift and verify your GPS location.</>
                  )}
                </p>
              </div>

              <Button
                variant={isCheckedIn ? 'danger' : 'success'}
                size="lg"
                className="font-black px-8"
                leftIcon={isCheckedIn ? <Square className="w-5 h-5" /> : <Play className="w-5 h-5" />}
                onClick={isCheckedIn ? handleCheckOut : handleCheckIn}
                isLoading={checkInMutation.isPending || checkOutMutation.isPending}
              >
                {isCheckedIn ? 'Clock Out Shift' : 'Clock In Shift (GPS)'}
              </Button>
            </div>
          </CardContent>
          {isCheckedIn && (
            <div className="absolute top-0 left-0 w-1 h-full bg-sky-500 animate-pulse" />
          )}
        </Card>
      )}

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              {!isStaff && <TableHead>Healthcare Professional</TableHead>}
              <TableHead>Shift Date</TableHead>
              <TableHead>Check In / Out</TableHead>
              <TableHead>Working Hours</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Notes</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(attendanceList || (todayAttendance ? [todayAttendance] : [])).map((att: AttendanceResponse) => {
              const { label: statusLabel, variant } = attendanceStatusDisplay(att.status);
              return (
                <TableRow key={att.id}>
                  {!isStaff && (
                    <TableCell>
                      <p className="font-bold text-slate-900 dark:text-slate-100">{att.staffName}</p>
                      <p className="text-xs text-slate-500">{att.staffEmail}</p>
                    </TableCell>
                  )}
                  <TableCell>{att.date}</TableCell>
                  <TableCell>
                    <p className="font-bold text-xs">
                      {att.checkInTime ? new Date(att.checkInTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—'}
                      {' → '}
                      {att.checkOutTime ? new Date(att.checkOutTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Active'}
                    </p>
                  </TableCell>
                  <TableCell>
                    <p className="font-bold text-sky-600 dark:text-sky-400">
                      {att.workingHours ? `${att.workingHours.toFixed(1)} hrs` : '—'}
                    </p>
                  </TableCell>
                  <TableCell>
                    <Badge variant={variant} size="sm" dot>
                      {statusLabel}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <span className="text-xs text-slate-500">{att.notes || '—'}</span>
                  </TableCell>
                </TableRow>
              );
            })}
            {(!attendanceList || attendanceList.length === 0) && !todayAttendance && (
              <TableRow>
                <TableCell colSpan={isStaff ? 5 : 6} className="text-center py-12 text-slate-500">
                  No attendance records found.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Card>
    </div>
  );
};
