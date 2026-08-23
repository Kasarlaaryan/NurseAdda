import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../store/useAuthStore';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { useToast } from '../components/common/Toast';
import { attendanceService, AttendanceResponse } from '../services/attendanceService';
import { assignmentService, AssignmentResponse } from '../services/assignmentService';
import {
  Clock,
  ShieldCheck,
  Play,
  Square,
  KeyRound,
  Mail,
  CheckCircle2,
} from 'lucide-react';

function attendanceStatusDisplay(status: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    CHECKED_IN: { label: 'Checked In', variant: 'success' },
    CHECKED_OUT: { label: 'Checked Out', variant: 'neutral' },
    OTP_PENDING: { label: 'OTP Pending', variant: 'warning' },
    LATE: { label: 'Late', variant: 'warning' },
    ABSENT: { label: 'Absent', variant: 'danger' },
    OVERTIME: { label: 'Overtime', variant: 'info' },
  };
  return map[status] || { label: status, variant: 'info' as const };
}

export const AttendancePage: React.FC = () => {
  const { activeRole } = useAuthStore();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const isStaff = activeRole === 'ROLE_STAFF';

  // ─── Flow state ───────────────────────────────────────
  type FlowStep = 'idle' | 'checkin-otp-sent' | 'checkout-otp-sent';
  const [flowStep, setFlowStep] = useState<FlowStep>('idle');
  const [otp, setOtp] = useState('');
  const [activeAssignmentId, setActiveAssignmentId] = useState<number | null>(null);
  const [activeAttendanceId, setActiveAttendanceId] = useState<number | null>(null);

  // ─── Queries ──────────────────────────────────────────
  const { data: todayAttendance, isLoading: loadingToday } = useQuery({
    queryKey: ['attendance-today'],
    queryFn: () => attendanceService.getToday(),
    enabled: isStaff,
    retry: false,
  });

  const { data: attendanceList, isLoading: loadingHistory } = useQuery({
    queryKey: ['attendance-my'],
    queryFn: () => attendanceService.getMyAttendance(),
    enabled: isStaff,
  });

  const { data: assignmentsData } = useQuery({
    queryKey: ['assignments-active'],
    queryFn: () => assignmentService.getAll(0, 50),
    enabled: isStaff,
  });

  const isCheckedIn = todayAttendance && !todayAttendance.checkOutTime && todayAttendance.status === 'CHECKED_IN';
  const hasCheckedOut = todayAttendance && todayAttendance.checkOutTime;

  // ─── Mutations ────────────────────────────────────────
  const requestCheckInOtpMutation = useMutation({
    mutationFn: (assignmentId: number) => attendanceService.requestCheckInOtp(assignmentId),
    onSuccess: () => {
      setFlowStep('checkin-otp-sent');
      showToast('success', 'OTP Sent', 'A one-time OTP has been sent to the client. Ask them to share it with you.');
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Failed', err?.response?.data?.message || 'Could not send OTP.');
    },
  });

  const checkInMutation = useMutation({
    mutationFn: (data: { assignmentId: number; otp: string }) => attendanceService.checkIn(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance-today'] });
      queryClient.invalidateQueries({ queryKey: ['attendance-my'] });
      setFlowStep('idle');
      setOtp('');
      setActiveAssignmentId(null);
      showToast('success', 'Checked In', 'OTP verified! Your shift has started.');
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Check-In Failed', err?.response?.data?.message || 'Invalid OTP.');
    },
  });

  const requestCheckOutOtpMutation = useMutation({
    mutationFn: (attendanceId: number) => attendanceService.requestCheckOutOtp(attendanceId),
    onSuccess: () => {
      setFlowStep('checkout-otp-sent');
      showToast('success', 'OTP Sent', 'A one-time checkout OTP has been sent to the client.');
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Failed', err?.response?.data?.message || 'Could not send checkout OTP.');
    },
  });

  const checkOutMutation = useMutation({
    mutationFn: (data: { attendanceId: number; otp: string }) =>
      attendanceService.checkOut(data.attendanceId, { otp: data.otp }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance-today'] });
      queryClient.invalidateQueries({ queryKey: ['attendance-my'] });
      setFlowStep('idle');
      setOtp('');
      setActiveAttendanceId(null);
      showToast('info', 'Checked Out', 'OTP verified! Your shift has ended.');
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Check-Out Failed', err?.response?.data?.message || 'Invalid checkout OTP.');
    },
  });

  // ─── Handlers ─────────────────────────────────────────
  const handleRequestCheckInOtp = () => {
    const activeAssignments = assignmentsData?.content?.filter(
      (a: AssignmentResponse) => a.status === 'ACTIVE' || a.status === 'ACCEPTED'
    );
    if (!activeAssignments || activeAssignments.length === 0) {
      showToast('error', 'No Active Assignment', 'You need an active assignment to check in.');
      return;
    }
    setActiveAssignmentId(activeAssignments[0].id);
    requestCheckInOtpMutation.mutate(activeAssignments[0].id);
  };

  const handleVerifyCheckInOtp = () => {
    if (!activeAssignmentId) return;
    if (otp.length !== 6) {
      showToast('error', 'Invalid OTP', 'Please enter the 6-digit OTP from the client.');
      return;
    }
    checkInMutation.mutate({ assignmentId: activeAssignmentId, otp });
  };

  const handleRequestCheckOutOtp = () => {
    if (!todayAttendance) return;
    setActiveAttendanceId(todayAttendance.id);
    requestCheckOutOtpMutation.mutate(todayAttendance.id);
  };

  const handleVerifyCheckOutOtp = () => {
    if (!activeAttendanceId) return;
    if (otp.length !== 6) {
      showToast('error', 'Invalid OTP', 'Please enter the 6-digit checkout OTP from the client.');
      return;
    }
    checkOutMutation.mutate({ attendanceId: activeAttendanceId, otp });
  };

  const handleCancel = () => {
    setFlowStep('idle');
    setOtp('');
    setActiveAssignmentId(null);
    setActiveAttendanceId(null);
  };

  const isLoading = loadingToday || loadingHistory;
  if (isLoading) {
    return <div className="flex items-center justify-center h-40"><Loader /></div>;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title={isStaff ? 'My Shift Attendance' : 'Attendance & Timesheet Tracking'}
        description={isStaff
          ? 'Request OTP from client to verify your check-in and check-out.'
          : 'Real-time shift check-in, OTP verification, overtime calculations, and supervisor approval logs.'
        }
      />

      {/* ─── Staff Check-In / Check-Out Control ─────── */}
      {isStaff && (
        <Card className="bg-gradient-to-r from-amber-900 to-slate-900 text-white border-amber-800 overflow-hidden relative">
          <CardContent className="p-6">

            {/* STATE: Idle — No check-in yet */}
            {flowStep === 'idle' && !isCheckedIn && !hasCheckedOut && (
              <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <ShieldCheck className="w-5 h-5 text-amber-400" />
                    <h3 className="font-bold text-base">OTP-Verified Check-In</h3>
                  </div>
                  <p className="text-xs text-slate-300">
                    Click below to request a one-time OTP from the client. No resend option.
                  </p>
                </div>
                <Button
                  variant="success"
                  size="lg"
                  className="font-black px-8"
                  leftIcon={<Play className="w-5 h-5" />}
                  onClick={handleRequestCheckInOtp}
                  isLoading={requestCheckInOtpMutation.isPending}
                >
                  Request Check-In OTP
                </Button>
              </div>
            )}

            {/* STATE: Check-in OTP sent — waiting for verification */}
            {flowStep === 'checkin-otp-sent' && (
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <div className="p-3 rounded-2xl bg-amber-500/20 text-amber-300">
                    <KeyRound className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-base">Enter Check-In OTP</h3>
                    <p className="text-xs text-slate-300">
                      Ask the client for the 6-digit code. One-time only — no resend.
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3 p-3 rounded-xl bg-amber-500/10 border border-amber-500/30">
                  <Mail className="w-4 h-4 text-amber-400 shrink-0" />
                  <p className="text-xs text-amber-200">
                    OTP sent to client's email. Also visible on their assignment detail page.
                  </p>
                </div>

                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  placeholder="000000"
                  maxLength={6}
                  className="w-full text-center text-3xl font-mono font-black tracking-[0.5em] py-4 rounded-xl bg-white/10 border border-white/20 text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-amber-400"
                />

                <div className="flex items-center gap-3">
                  <Button
                    variant="success"
                    size="lg"
                    className="flex-1 font-black"
                    leftIcon={<CheckCircle2 className="w-5 h-5" />}
                    onClick={handleVerifyCheckInOtp}
                    isLoading={checkInMutation.isPending}
                    disabled={otp.length !== 6}
                  >
                    Verify & Check In
                  </Button>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  className="text-slate-400 hover:text-white"
                  onClick={handleCancel}
                >
                  Cancel
                </Button>
              </div>
            )}

            {/* STATE: Checked in — show check-out option */}
            {flowStep === 'idle' && isCheckedIn && (
              <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-5 h-5 text-emerald-400" />
                    <h3 className="font-bold text-base">Checked In</h3>
                  </div>
                  <p className="text-xs text-slate-300">
                    Shift started at <span className="font-bold text-amber-300">{todayAttendance?.checkInTime}</span>
                  </p>
                </div>
                <Button
                  variant="danger"
                  size="lg"
                  className="font-black px-8"
                  leftIcon={<Square className="w-5 h-5" />}
                  onClick={handleRequestCheckOutOtp}
                  isLoading={requestCheckOutOtpMutation.isPending}
                >
                  Request Check-Out OTP
                </Button>
              </div>
            )}

            {/* STATE: Check-out OTP sent — waiting for verification */}
            {flowStep === 'checkout-otp-sent' && (
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <div className="p-3 rounded-2xl bg-rose-500/20 text-rose-300">
                    <KeyRound className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-base">Enter Check-Out OTP</h3>
                    <p className="text-xs text-slate-300">
                      Ask the client for the 6-digit checkout code. One-time only — no resend.
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30">
                  <Mail className="w-4 h-4 text-rose-400 shrink-0" />
                  <p className="text-xs text-rose-200">
                    Checkout OTP sent to client's email. Also visible on their assignment detail page.
                  </p>
                </div>

                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  placeholder="000000"
                  maxLength={6}
                  className="w-full text-center text-3xl font-mono font-black tracking-[0.5em] py-4 rounded-xl bg-white/10 border border-white/20 text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-rose-400"
                />

                <div className="flex items-center gap-3">
                  <Button
                    variant="danger"
                    size="lg"
                    className="flex-1 font-black"
                    leftIcon={<Square className="w-5 h-5" />}
                    onClick={handleVerifyCheckOutOtp}
                    isLoading={checkOutMutation.isPending}
                    disabled={otp.length !== 6}
                  >
                    Verify & Check Out
                  </Button>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  className="text-slate-400 hover:text-white"
                  onClick={handleCancel}
                >
                  Cancel
                </Button>
              </div>
            )}

            {/* STATE: Checked out — done */}
            {flowStep === 'idle' && hasCheckedOut && (
              <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-5 h-5 text-slate-400" />
                    <h3 className="font-bold text-base">Shift Complete</h3>
                  </div>
                  <p className="text-xs text-slate-300">
                    Checked out at <span className="font-bold text-slate-300">{todayAttendance?.checkOutTime}</span>
                    {todayAttendance?.workingHours && (
                      <> — {todayAttendance.workingHours.toFixed(1)} hours worked</>
                    )}
                  </p>
                </div>
                <Badge variant="neutral" size="md">Completed</Badge>
              </div>
            )}
          </CardContent>
          {isCheckedIn && (
            <div className="absolute top-0 left-0 w-1 h-full bg-emerald-500 animate-pulse" />
          )}
        </Card>
      )}

      {/* ─── Attendance History Table ─────────────────── */}
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
                    <p className="font-bold text-amber-600 dark:text-amber-400">
                      {att.workingHours ? `${att.workingHours.toFixed(1)} hrs` : '—'}
                    </p>
                  </TableCell>
                  <TableCell>
                    <Badge variant={variant} size="sm" dot>{statusLabel}</Badge>
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
