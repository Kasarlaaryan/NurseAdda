import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Avatar } from '../components/common/Avatar';
import { Badge } from '../components/common/Badge';
import { useAuthStore } from '../store/useAuthStore';
import { useToast } from '../components/common/Toast';
import { USER_ROLES_CONFIG } from '../constants';
import { staffService } from '../services/staffService';
import {
  User,
  Mail,
  Phone,
  Shield,
  Lock,
  GraduationCap,
  MapPin,
  CreditCard,
  FileText,
  ExternalLink,
  CheckCircle2,
  Clock,
  AlertTriangle,
  AlertCircle,
  RefreshCw,
  Image as ImageIcon,
  File,
} from 'lucide-react';

/** Check if a file path points to an image */
function isImageFile(path: string): boolean {
  const ext = path.split('.').pop()?.toLowerCase() || '';
  return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext);
}

/** Extract filename from path */
function getFileName(path: string): string {
  return path.split('/').pop() || path;
}

/** Get file type icon */
function getFileIcon(path: string) {
  const ext = path.split('.').pop()?.toLowerCase() || '';
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return ImageIcon;
  return File;
}

/** License status display config */
function licenseStatusDisplay(status?: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'danger' | 'neutral'; icon: React.ElementType }> = {
    VALID: { label: 'Valid', variant: 'success', icon: CheckCircle2 },
    EXPIRING_SOON: { label: 'Expiring Soon', variant: 'warning', icon: AlertTriangle },
    EXPIRED: { label: 'Expired', variant: 'danger', icon: AlertTriangle },
    UNKNOWN: { label: 'Not Set', variant: 'neutral', icon: Clock },
  };
  return map[status || 'UNKNOWN'] || map.UNKNOWN;
}

/** Document preview card */
const DocumentCard: React.FC<{ path: string; label: string }> = ({ path, label }) => {
  const isImage = isImageFile(path);
  const fileName = getFileName(path);
  const FileIcon = getFileIcon(path);
  const fileUrl = `/${path}`;

  return (
    <div className="rounded-xl border border-slate-200 dark:border-slate-800 overflow-hidden">
      {isImage && (
        <div className="relative w-full h-32 bg-slate-100 dark:bg-slate-800">
          <img
            src={fileUrl}
            alt={label}
            className="w-full h-full object-contain"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none';
            }}
          />
        </div>
      )}
      <div className="p-3 flex items-center justify-between bg-slate-50 dark:bg-slate-800/50">
        <div className="flex items-center gap-2 min-w-0">
          <div className={`p-1.5 rounded-lg ${isImage ? 'bg-purple-500/10 text-purple-500' : 'bg-amber-500/10 text-amber-500'}`}>
            <FileIcon className="w-3.5 h-3.5" />
          </div>
          <div className="min-w-0">
            <p className="text-[11px] font-bold text-slate-900 dark:text-white truncate">{label}</p>
            <p className="text-[10px] text-slate-400 truncate">{fileName}</p>
          </div>
        </div>
        <a
          href={fileUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="shrink-0 p-1.5 rounded-lg hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors text-slate-400 hover:text-amber-600"
          title="Open in new tab"
        >
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>
    </div>
  );
};

export const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { user, activeRole } = useAuthStore();
  const { showToast } = useToast();
  const roleConfig = USER_ROLES_CONFIG[activeRole] || USER_ROLES_CONFIG['ROLE_SUPER_ADMIN'];
  const isStaff = activeRole === 'ROLE_STAFF';

  const { data: staffProfile } = useQuery({
    queryKey: ['my-staff-profile'],
    queryFn: () => staffService.getMyProfile(),
    enabled: isStaff,
    retry: false,
  });

  const handleUpdate = (e: React.FormEvent) => {
    e.preventDefault();
    showToast('success', 'Profile Updated', 'Your user information has been updated.');
  };

  const ls = licenseStatusDisplay(staffProfile?.licenseStatus);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Account & User Profile"
        description="Manage your personal details, role credentials & portal preferences."
      />

      <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
        {/* ─── Left Sidebar: Avatar & Quick Info ─── */}
        <Card className="md:col-span-4 text-center">
          <CardContent className="p-6 space-y-4">
            <Avatar name={user?.name || 'User'} src={user?.avatarUrl} size="xl" className="mx-auto" />
            <div>
              <h3 className="font-extrabold text-base text-slate-900 dark:text-slate-100">{user?.name}</h3>
              <p className="text-xs text-slate-500 mt-0.5">{user?.email}</p>
              <div className="mt-2 flex items-center justify-center gap-2 flex-wrap">
                <Badge variant="primary" size="md">{roleConfig.label}</Badge>
                {isStaff && staffProfile && (
                  <Badge
                    variant={staffProfile.verified ? 'success' : 'warning'}
                    size="md"
                    dot
                  >
                    {staffProfile.verified ? 'Verified' : 'Pending'}
                  </Badge>
                )}
              </div>
            </div>

            {/* Quick Info */}
            <div className="pt-4 border-t border-slate-100 dark:border-slate-800 text-xs text-left space-y-2">
              {isStaff && staffProfile?.staffCategory && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Category:</span>
                  <span className="font-semibold">{staffProfile.staffCategory}</span>
                </div>
              )}
              {isStaff && staffProfile?.location && (
                <div className="flex justify-between">
                  <span className="text-slate-500">Location:</span>
                  <span className="font-semibold">{staffProfile.location}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-slate-500">Department:</span>
                <span className="font-semibold">{user?.department || 'Executive Operations'}</span>
              </div>
              {isStaff && staffProfile && (
                <div className="flex justify-between items-center">
                  <span className="text-slate-500">License:</span>
                  <Badge variant={ls.variant} size="sm">{ls.label}</Badge>
                </div>
              )}
            </div>

            {/* Quick Action for Staff */}
            {isStaff && (
              <div className="pt-4 border-t border-slate-100 dark:border-slate-800">
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-xs"
                  onClick={() => navigate('/complete-profile')}
                  leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
                >
                  Update Profile & Documents
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* ─── Right: Details ─── */}
        <div className="md:col-span-8 space-y-6">
          {/* Personal Information */}
          <Card>
            <CardHeader>
              <CardTitle>Personal Information</CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleUpdate} className="space-y-4">
                <Input
                  label="Full Name"
                  defaultValue={user?.name}
                  leftIcon={<User className="w-4 h-4" />}
                  required
                />
                <div className="grid grid-cols-2 gap-3">
                  <Input
                    label="Email Address"
                    type="email"
                    defaultValue={user?.email}
                    leftIcon={<Mail className="w-4 h-4" />}
                    required
                  />
                  <Input
                    label="Phone Number"
                    defaultValue={user?.phone}
                    leftIcon={<Phone className="w-4 h-4" />}
                    required
                  />
                </div>
                <Button type="submit" variant="primary" size="sm" className="mt-2">
                  Save Profile Changes
                </Button>
              </form>
            </CardContent>
          </Card>

          {/* ─── Staff-Only: Professional Details ─── */}
          {isStaff && staffProfile && (
            <>
              {/* Verification / Rejection Status */}
              {staffProfile.rejectionReason && !staffProfile.verified && (
                <Card className="border-l-4 border-l-rose-500">
                  <CardContent className="p-5">
                    <div className="flex items-start gap-3">
                      <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
                      <div>
                        <p className="text-xs font-bold text-rose-600 uppercase tracking-wider">Rejection Reason</p>
                        <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">{staffProfile.rejectionReason}</p>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="mt-3 text-xs"
                          onClick={() => navigate('/complete-profile')}
                          leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
                        >
                          Update Documents & Re-submit
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Professional Details */}
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between w-full">
                    <CardTitle className="flex items-center gap-2">
                      <GraduationCap className="w-4 h-4 text-purple-500" />
                      Professional Details
                    </CardTitle>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => navigate('/complete-profile')}
                    >
                      Edit
                    </Button>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-3 rounded-xl border border-slate-100 dark:border-slate-800">
                      <div className="flex items-center gap-2 text-slate-500 mb-1">
                        <CreditCard className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-semibold uppercase tracking-wider">Aadhar Number</span>
                      </div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white font-mono">
                        {staffProfile.aadharCardNumber || '—'}
                      </p>
                    </div>
                    <div className="p-3 rounded-xl border border-slate-100 dark:border-slate-800">
                      <div className="flex items-center gap-2 text-slate-500 mb-1">
                        <MapPin className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-semibold uppercase tracking-wider">Location</span>
                      </div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {staffProfile.location || '—'}
                      </p>
                    </div>
                    <div className="p-3 rounded-xl border border-slate-100 dark:border-slate-800">
                      <div className="flex items-center gap-2 text-slate-500 mb-1">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-semibold uppercase tracking-wider">License Validity</span>
                      </div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {staffProfile.licenseValidityDate || '—'}
                      </p>
                    </div>
                    <div className="p-3 rounded-xl border border-slate-100 dark:border-slate-800">
                      <div className="flex items-center gap-2 text-slate-500 mb-1">
                        <RefreshCw className="w-3.5 h-3.5" />
                        <span className="text-[10px] font-semibold uppercase tracking-wider">License Renewal</span>
                      </div>
                      <p className="text-sm font-bold text-slate-900 dark:text-white">
                        {staffProfile.licenseRenewalDate || '—'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Uploaded Documents */}
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between w-full">
                    <CardTitle className="flex items-center gap-2">
                      <FileText className="w-4 h-4 text-amber-500" />
                      Uploaded Documents
                    </CardTitle>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => navigate('/complete-profile')}
                    >
                      Re-upload
                    </Button>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {staffProfile.stateBoardCertificatePath && (
                      <DocumentCard path={staffProfile.stateBoardCertificatePath} label="Professional License" />
                    )}
                    {(staffProfile.educationalDocumentPaths || []).map((docPath, idx) => (
                      <DocumentCard key={`edu-${idx}`} path={docPath} label={`Educational Document ${idx + 1}`} />
                    ))}
                    {(staffProfile.photoPaths || []).map((photoPath, idx) => (
                      <DocumentCard key={`photo-${idx}`} path={photoPath} label={`Photo ${idx + 1}`} />
                    ))}
                    {!staffProfile.stateBoardCertificatePath &&
                      (staffProfile.educationalDocumentPaths || []).length === 0 &&
                      (staffProfile.photoPaths || []).length === 0 && (
                        <div className="col-span-2 text-center py-8 text-slate-400">
                          <FileText className="w-8 h-8 mx-auto mb-2 opacity-50" />
                          <p className="text-xs">No documents uploaded yet.</p>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="mt-2"
                            onClick={() => navigate('/complete-profile')}
                          >
                            Upload Documents
                          </Button>
                        </div>
                      )}
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
