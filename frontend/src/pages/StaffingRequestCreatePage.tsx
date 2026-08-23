import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { useToast } from '../components/common/Toast';
import { staffingRequestService } from '../services/assignmentService';
import { Clock, ChevronLeft, Send, MapPin } from 'lucide-react';

export const StaffingRequestCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const queryClient = useQueryClient();

  // Form State
  const [formDesignation, setFormDesignation] = useState('Registered Nurse (RN)');
  const [formLocation, setFormLocation] = useState('');
  const [formLocationLink, setFormLocationLink] = useState('');
  const [formRequestType, setFormRequestType] = useState<'ON_CALL' | 'MONTHLY'>('ON_CALL');
  const [formShift, setFormShift] = useState('Night Shift');
  const [formStartDate, setFormStartDate] = useState('');
  const [formEndDate, setFormEndDate] = useState('');
  const [formStartTime, setFormStartTime] = useState('');
  const [formEndTime, setFormEndTime] = useState('');
  const [formNumberOfStaff, setFormNumberOfStaff] = useState(1);
  const [formRequiredSkills, setFormRequiredSkills] = useState('');

  // ─── Create mutation ─────────────────────────────────
  const createMutation = useMutation({
    mutationFn: staffingRequestService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staffing-requests'] });
      showToast('success', 'Request Created', 'Staffing request has been broadcast to available staff.');
      navigate('/requests');
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      showToast('error', 'Failed', err?.response?.data?.message || 'Could not create request.');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validate: request must be made at least 4 hours before shift start
    if (formStartDate && formStartTime) {
      const now = new Date();
      const [startH, startM] = formStartTime.split(':').map(Number);
      const shiftStart = new Date(formStartDate);
      shiftStart.setHours(startH, startM, 0, 0);
      const fourHoursBefore = new Date(shiftStart.getTime() - 4 * 60 * 60 * 1000);

      if (now > shiftStart) {
        showToast('error', 'Invalid Time', 'Cannot request staff for a past shift time.');
        return;
      }
      if (now > fourHoursBefore) {
        showToast(
          'error',
          'Too Late',
          `You must request staff at least 4 hours before the shift starts (${formStartTime}). Request deadline was ${fourHoursBefore.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}.`,
        );
        return;
      }
    }

    // Night shift rule: after 5 PM, requests go to next day
    if (formStartDate && formStartTime) {
      const [startH] = formStartTime.split(':').map(Number);
      const now = new Date();
      const requestDate = new Date(formStartDate);
      requestDate.setHours(0, 0, 0, 0);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (requestDate.getTime() === today.getTime() && now.getHours() >= 17 && startH < 12) {
        showToast(
          'info',
          'Night Shift Rule',
          'After 5 PM, same-day night shift requests are closed. The shift has been moved to the next day.',
        );
        const nextDay = new Date(requestDate);
        nextDay.setDate(nextDay.getDate() + 1);
        setFormStartDate(nextDay.toISOString().split('T')[0]);
      }
    }

    createMutation.mutate({
      designation: formDesignation,
      location: formLocation,
      locationLink: formLocationLink || undefined,
      requestType: formRequestType,
      shift: formShift,
      startDate: formStartDate,
      endDate: formEndDate,
      startTime: formStartTime,
      endTime: formEndTime,
      numberOfStaff: formNumberOfStaff,
      requiredSkills: formRequiredSkills,
    });
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => navigate('/requests')}
          className="rounded-full w-9 h-9 p-0 flex items-center justify-center"
        >
          <ChevronLeft className="w-5 h-5" />
        </Button>
        <PageHeader
          title="New Staffing Request"
          description="Fill in the details below to create a new staffing shift request."
        />
      </div>

      <form onSubmit={handleSubmit}>
        <Card className="border-slate-200 dark:border-slate-800">
          <CardContent className="p-6 space-y-6">

            {/* ─── Section: Role & Location ─── */}
            <div className="space-y-4">
              <h3 className="text-xs font-black uppercase tracking-widest text-slate-400 dark:text-slate-500">
                Role & Location
              </h3>
              <div>
                <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-1">
                  Designation / Role
                </label>
                <select
                  className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5 focus:ring-1 focus:ring-amber-500 outline-none"
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
              <div className="space-y-1">
                <Input
                  label="Google Maps Location Link"
                  type="url"
                  value={formLocationLink}
                  onChange={(e) => setFormLocationLink(e.target.value)}
                  placeholder="https://maps.app.goo.gl/... or Google Maps URL"
                  leftIcon={<MapPin className="w-4 h-4" />}
                />
                <p className="text-[10px] text-slate-400">
                  Paste a Google Maps share link so staff can navigate to the exact location.
                </p>
              </div>
            </div>

            {/* ─── Section: Request Type & Shift ─── */}
            <div className="space-y-4">
              <h3 className="text-xs font-black uppercase tracking-widest text-slate-400 dark:text-slate-500">
                Request Type & Shift
              </h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-1">
                    Request Type
                  </label>
                  <select
                    className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5 focus:ring-1 focus:ring-amber-500 outline-none"
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
                    className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5 focus:ring-1 focus:ring-amber-500 outline-none"
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
            </div>

            {/* ─── Section: Date & Time ─── */}
            <div className="space-y-4">
              <h3 className="text-xs font-black uppercase tracking-widest text-slate-400 dark:text-slate-500">
                Schedule (Date & Time)
              </h3>
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="Start Date"
                  type="date"
                  value={formStartDate}
                  onChange={(e) => setFormStartDate(e.target.value)}
                  required
                />
                <Input
                  label="Start Time"
                  type="time"
                  value={formStartTime}
                  onChange={(e) => setFormStartTime(e.target.value)}
                  required
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="End Date"
                  type="date"
                  value={formEndDate}
                  onChange={(e) => setFormEndDate(e.target.value)}
                  required
                />
                <Input
                  label="End Time"
                  type="time"
                  value={formEndTime}
                  onChange={(e) => setFormEndTime(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* ─── Section: Staff & Skills ─── */}
            <div className="space-y-4">
              <h3 className="text-xs font-black uppercase tracking-widest text-slate-400 dark:text-slate-500">
                Staffing Details
              </h3>
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
            </div>

            {/* ─── Info Box ─── */}
            <div className="flex items-start gap-3 p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800/30">
              <Clock className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
              <div className="text-[11px] text-amber-700 dark:text-amber-400 leading-relaxed">
                <p className="font-bold mb-1">Important Timing Rules</p>
                <ul className="list-disc list-inside space-y-0.5">
                  <li>Requests must be made at least <strong>4 hours</strong> before the shift start time.</li>
                  <li>After <strong>5:00 PM</strong>, same-day requests are closed and will be scheduled for the next day.</li>
                  <li>Cancellation <strong>30+ min before</strong> shift = 80% refund; within 30 min = 20% refund.</li>
                </ul>
              </div>
            </div>

            {/* ─── Actions ─── */}
            <div className="flex items-center justify-between pt-4 border-t border-slate-100 dark:border-slate-800">
              <Button
                variant="ghost"
                size="sm"
                type="button"
                onClick={() => navigate('/requests')}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                size="lg"
                type="submit"
                isLoading={createMutation.isPending}
                leftIcon={<Send className="w-4 h-4" />}
                className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold min-w-[200px]"
              >
                Submit Staffing Request
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
};
