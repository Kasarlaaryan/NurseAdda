import React, { useEffect, useRef } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../store/useAuthStore';
import { staffService } from '../services/staffService';
import { notificationService } from '../services/notificationService';
import { useToast } from '../components/common/Toast';
import { Loader } from '../components/common/Loader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Clock, ShieldCheck, FileCheck, RefreshCw, AlertTriangle, AlertCircle, BadgeCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

/**
 * Route guard that blocks ROLE_STAFF users whose profile has not been
 * verified by an admin/super-admin.
 *
 * Allowed to pass through:
 *   - All non-staff roles (admin, super-admin, client)
 *   - Staff with verified === true
 *
 * Blocked staff see a "Pending Verification" holding page instead
 * of the requested route.
 */
export const StaffVerificationGuard: React.FC = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();

  const isStaff = user?.role === 'ROLE_STAFF';

  // Non-staff roles pass through immediately
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const wasVerifiedRef = useRef<boolean | null>(null);

  const { data: profile, isLoading } = useQuery({
    queryKey: ['my-staff-profile'],
    queryFn: () => staffService.getMyProfile(),
    enabled: isStaff,
    retry: false,
    // Poll every 15 seconds to detect verification changes
    refetchInterval: isStaff ? 15000 : false,
  });

  // Detect when staff gets verified and show celebration toast
  useEffect(() => {
    if (!profile || !isStaff) return;

    const currentVerified = profile.verified;
    const previousVerified = wasVerifiedRef.current;

    // First load — just record the value
    if (previousVerified === null) {
      wasVerifiedRef.current = currentVerified;
      return;
    }

    // Status changed from unverified → verified
    if (!previousVerified && currentVerified) {
      showToast(
        'success',
        '🎉 Profile Verified!',
        'Your account has been approved. You can now accept staffing assignments!'
      );
      // Stop polling and refetch fresh data
      queryClient.invalidateQueries({ queryKey: ['my-staff-profile'] });
    }

    wasVerifiedRef.current = currentVerified;
  }, [profile, isStaff, showToast, queryClient]);

  // Not a staff member → allow
  if (!isStaff) {
    return <Outlet />;
  }

  // Still loading profile
  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader />
      </div>
    );
  }

  // Staff profile not found → redirect to complete profile
  if (!profile) {
    return <Navigate to="/complete-profile" replace />;
  }

  // Check if documents have been submitted (any document path is non-empty)
  const hasSubmittedDocuments = Boolean(
    profile.stateBoardCertificatePath ||
    (profile.educationalDocumentPaths && profile.educationalDocumentPaths.length > 0) ||
    (profile.photoPaths && profile.photoPaths.length > 0)
  );

  // Profile exists but no documents submitted yet → redirect to complete profile
  if (!hasSubmittedDocuments && !profile.verified) {
    return <Navigate to="/complete-profile" replace />;
  }

  // Documents submitted but not yet verified by admin → show pending page
  if (!profile.verified) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Card className="max-w-md w-full border-amber-200 dark:border-amber-800">
          <CardContent className="p-8 text-center space-y-5">
            <div className="w-16 h-16 rounded-2xl bg-amber-50 dark:bg-amber-500/10 text-amber-500 flex items-center justify-center mx-auto">
              <Clock className="w-8 h-8" />
            </div>
            <div className="space-y-2">
              <h2 className="text-xl font-black text-slate-900 dark:text-white">
                Verification Pending
              </h2>
              <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
                Your documents have been submitted and are under review.
                An admin will verify your credentials shortly.
              </p>
            </div>

            <div className="space-y-3 text-left">
              {hasSubmittedDocuments && (
                <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                  <FileCheck className="w-5 h-5 text-amber-500 shrink-0" />
                  <div>
                    <p className="text-xs font-bold text-slate-900 dark:text-white">Documents Submitted</p>
                    <p className="text-[10px] text-slate-500">License, ID proof, police clearance</p>
                  </div>
                </div>
              )}
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                <ShieldCheck className="w-5 h-5 text-amber-500 shrink-0" />
                <div>
                  <p className="text-xs font-bold text-slate-900 dark:text-white">Awaiting Admin Approval</p>
                  <p className="text-[10px] text-slate-500">You'll be notified once verified</p>
                </div>
              </div>

              {/* Show rejection reason if documents were previously rejected */}
              {profile.rejectionReason && (
                <div className="flex items-start gap-3 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-800">
                  <AlertCircle className="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-[10px] font-bold text-rose-600 uppercase tracking-wider">Rejection Reason</p>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">{profile.rejectionReason}</p>
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center gap-3">
              <Button
                variant="ghost"
                size="sm"
                className="text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
                onClick={() => navigate('/dashboard')}
              >
                Back to Dashboard
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={() => navigate('/complete-profile')}
                leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
              >
                Update Documents
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Verified but license expired → block access
  if (profile.verified && profile.licenseStatus === 'EXPIRED') {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Card className="max-w-md w-full border-rose-200 dark:border-rose-800">
          <CardContent className="p-8 text-center space-y-5">
            <div className="w-16 h-16 rounded-2xl bg-rose-50 dark:bg-rose-500/10 text-rose-500 flex items-center justify-center mx-auto">
              <AlertTriangle className="w-8 h-8" />
            </div>
            <div className="space-y-2">
              <h2 className="text-xl font-black text-slate-900 dark:text-white">
                License Expired
              </h2>
              <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
                Your professional license has expired. Please renew your license and update your profile to continue receiving assignments.
              </p>
            </div>

            <div className="space-y-3 text-left">
              <div className="flex items-center gap-3 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-800">
                <AlertTriangle className="w-5 h-5 text-rose-500 shrink-0" />
                <div>
                  <p className="text-xs font-bold text-rose-700 dark:text-rose-300">License Expired</p>
                  <p className="text-[10px] text-rose-500">Valid until: {profile.licenseValidityDate}</p>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Button
                variant="ghost"
                size="sm"
                className="text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
                onClick={() => navigate('/dashboard')}
              >
                Back to Dashboard
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={() => navigate('/complete-profile')}
                leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
              >
                Renew License
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Verified but license expiring soon → allow with warning banner
  if (profile.verified && profile.licenseStatus === 'EXPIRING_SOON') {
    return (
      <div className="space-y-4">
        <div className="mx-auto max-w-2xl">
          <Card className="border-amber-200 dark:border-amber-800 bg-amber-50/50 dark:bg-amber-950/20">
            <CardContent className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-amber-100 dark:bg-amber-900/40 text-amber-600 flex items-center justify-center shrink-0">
                  <AlertTriangle className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs font-bold text-slate-900 dark:text-white">License Expiring Soon</p>
                  <p className="text-[10px] text-slate-500">Valid until: {profile.licenseValidityDate}</p>
                </div>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => navigate('/complete-profile')}
              >
                Update
              </Button>
            </CardContent>
          </Card>
        </div>
        <Outlet />
      </div>
    );
  }

  // Verified and license valid → allow access
  return <Outlet />;
};
