import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { staffService } from '../services/staffService';
import { StaffProfileResponse } from '../services/authService';
import { notificationService } from '../services/notificationService';
import { useToast } from '../components/common/Toast';
import { Modal } from '../components/common/Modal';
import {
  ShieldCheck,
  Clock,
  CheckCircle2,
  XCircle,
  FileText,
  Eye,
  AlertCircle,
  Search,
  Briefcase,
  Mail,
  Phone,
  MessageSquare,
  ExternalLink,
  Image as ImageIcon,
  File,
  FileBadge,
  CreditCard,
  UserCheck,
  Check,
  X,
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

/** Get file extension icon */
function getFileIcon(path: string) {
  const ext = path.split('.').pop()?.toLowerCase() || '';
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) {
    return ImageIcon;
  }
  return File;
}

/** Document status type */
type DocStatus = 'pending' | 'approved' | 'rejected';

/** Document with metadata */
interface DocItem {
  id: string;
  path: string;
  label: string;
  icon: React.ElementType;
  status: DocStatus;
  rejectReason?: string;
}

/** Document preview card with approve/reject buttons */
const DocumentReviewCard: React.FC<{
  doc: DocItem;
  onApprove: (id: string) => void;
  onReject: (id: string) => void;
  rejectReason: string;
  onRejectReasonChange: (id: string, reason: string) => void;
  showRejectInput: string | null;
  setShowRejectInput: (id: string | null) => void;
}> = ({ doc, onApprove, onReject, rejectReason, onRejectReasonChange, showRejectInput, setShowRejectInput }) => {
  const isImage = isImageFile(doc.path);
  const fileName = getFileName(doc.path);
  const FileIcon = getFileIcon(doc.path);
  const fileUrl = `/${doc.path}`;

  const statusColors = {
    pending: 'border-slate-200 dark:border-slate-700',
    approved: 'border-emerald-300 dark:border-emerald-700 bg-emerald-50/30 dark:bg-emerald-950/20',
    rejected: 'border-rose-300 dark:border-rose-700 bg-rose-50/30 dark:bg-rose-950/20',
  };

  const statusBadge = {
    pending: <Badge variant="warning" size="sm">Pending Review</Badge>,
    approved: <Badge variant="success" size="sm"><Check className="w-3 h-3 mr-1 inline" />Approved</Badge>,
    rejected: <Badge variant="danger" size="sm"><X className="w-3 h-3 mr-1 inline" />Rejected</Badge>,
  };

  return (
    <div className={`rounded-2xl border-2 overflow-hidden transition-all ${statusColors[doc.status]}`}>
      {/* Image preview */}
      {isImage && (
        <div className="relative w-full h-44 bg-slate-100 dark:bg-slate-800">
          <img
            src={fileUrl}
            alt={doc.label}
            className="w-full h-full object-contain"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none';
            }}
          />
          <a
            href={fileUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="absolute top-2 right-2 p-1.5 rounded-lg bg-white/90 dark:bg-slate-900/90 backdrop-blur-sm border border-slate-200 dark:border-slate-700 text-slate-500 hover:text-amber-600 transition-colors"
            title="Open full size"
          >
            <ExternalLink className="w-3.5 h-3.5" />
          </a>
        </div>
      )}

      <div className="p-4 space-y-3">
        {/* File info */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className={`p-2 rounded-xl ${isImage ? 'bg-purple-500/10 text-purple-500' : 'bg-amber-500/10 text-amber-500'}`}>
              <doc.icon className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-900 dark:text-white">{doc.label}</p>
              <p className="text-[10px] text-slate-400 truncate max-w-[200px]">{fileName}</p>
            </div>
          </div>
          {statusBadge[doc.status]}
        </div>

        {/* Approve / Reject buttons */}
        {doc.status === 'pending' && (
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Button
                variant="ghost"
                size="sm"
                className="flex-1 text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800"
                onClick={() => onApprove(doc.id)}
                leftIcon={<CheckCircle2 className="w-3.5 h-3.5" />}
              >
                Approve
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="flex-1 text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 border border-rose-200 dark:border-rose-800"
                onClick={() => setShowRejectInput(showRejectInput === doc.id ? null : doc.id)}
                leftIcon={<XCircle className="w-3.5 h-3.5" />}
              >
                Reject
              </Button>
            </div>

            {/* Reject reason input */}
            {showRejectInput === doc.id && (
              <div className="space-y-2 animate-in fade-in slide-in-from-top-1 duration-200">
                <textarea
                  className="w-full rounded-xl border border-rose-200 dark:border-rose-800 bg-white dark:bg-slate-900 text-xs p-2.5 text-slate-900 dark:text-white focus:ring-1 focus:ring-rose-500 outline-none resize-none"
                  rows={2}
                  placeholder="Reason for rejection (e.g., blurry image, expired document)..."
                  value={rejectReason}
                  onChange={(e) => onRejectReasonChange(doc.id, e.target.value)}
                />
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 border border-rose-200 dark:border-rose-800"
                  onClick={() => onReject(doc.id)}
                  leftIcon={<XCircle className="w-3.5 h-3.5" />}
                >
                  Confirm Rejection
                </Button>
              </div>
            )}
          </div>
        )}

        {/* Show rejection reason for rejected docs */}
        {doc.status === 'rejected' && doc.rejectReason && (
          <div className="p-2 rounded-lg bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-800">
            <p className="text-[10px] font-bold text-rose-600 uppercase tracking-wider">Rejection Reason</p>
            <p className="text-[11px] text-rose-700 dark:text-rose-300 mt-0.5">{doc.rejectReason}</p>
          </div>
        )}

        {/* Non-image: open in new tab */}
        {!isImage && (
          <a
            href={fileUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 text-[10px] font-bold text-amber-500 hover:text-amber-600 hover:underline"
          >
            <ExternalLink className="w-3 h-3" />
            Open document in new tab
          </a>
        )}
      </div>
    </div>
  );
};

function staffDisplayName(s: StaffProfileResponse) {
  return `${s.firstName} ${s.lastName}`;
}

export const VerificationManagementPage: React.FC = () => {
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStaff, setSelectedStaff] = useState<StaffProfileResponse | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [overallNotes, setOverallNotes] = useState('');
  const [showRejectInput, setShowRejectInput] = useState<string | null>(null);

  // Per-document status tracking
  const [docStatuses, setDocStatuses] = useState<Record<string, DocStatus>>({});
  const [docRejectReasons, setDocRejectReasons] = useState<Record<string, string>>({});

  const { data, isLoading } = useQuery({
    queryKey: ['staff-verification'],
    queryFn: () => staffService.getAllStaff(0, 100),
  });

  const verifyMutation = useMutation({
    mutationFn: ({ userId, verified, rejectionReason }: { userId: number; verified: boolean; rejectionReason?: string }) =>
      staffService.verifyStaff(userId, verified, rejectionReason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['staff-verification'] });
      queryClient.invalidateQueries({ queryKey: ['staff-management'] });
    },
  });

  const allStaff = data?.content || [];

  const filteredStaff = allStaff.filter((s) => {
    const fullName = staffDisplayName(s);
    return fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.email.toLowerCase().includes(searchTerm.toLowerCase());
  });

  const pendingStaff = filteredStaff.filter(s => !s.verified);
  const verifiedStaff = filteredStaff.filter(s => s.verified);

  /** Build doc items from selected staff */
  const buildDocItems = (staff: StaffProfileResponse): DocItem[] => {
    const docs: DocItem[] = [];
    if (staff.stateBoardCertificatePath) {
      docs.push({
        id: 'license',
        path: staff.stateBoardCertificatePath,
        label: 'Professional License',
        icon: FileBadge,
        status: docStatuses['license'] || 'pending',
        rejectReason: docRejectReasons['license'],
      });
    }
    (staff.educationalDocumentPaths || []).forEach((p, i) => {
      const id = `edu-${i}`;
      docs.push({
        id,
        path: p,
        label: i === 0 ? 'Police Clearance' : `Educational Document ${i + 1}`,
        icon: i === 0 ? ShieldCheck : FileText,
        status: docStatuses[id] || 'pending',
        rejectReason: docRejectReasons[id],
      });
    });
    (staff.photoPaths || []).forEach((p, i) => {
      const id = `photo-${i}`;
      docs.push({
        id,
        path: p,
        label: 'Identity Proof',
        icon: CreditCard,
        status: docStatuses[id] || 'pending',
        rejectReason: docRejectReasons[id],
      });
    });
    return docs;
  };

  const docItems = selectedStaff ? buildDocItems(selectedStaff) : [];
  const allApproved = docItems.length > 0 && docItems.every(d => d.status === 'approved');
  const hasRejected = docItems.some(d => d.status === 'rejected');
  const allReviewed = docItems.every(d => d.status !== 'pending');

  const handleDocApprove = (docId: string) => {
    setDocStatuses(prev => ({ ...prev, [docId]: 'approved' }));
    setDocRejectReasons(prev => ({ ...prev, [docId]: '' }));
    setShowRejectInput(null);
  };

  const handleDocReject = (docId: string) => {
    const reason = docRejectReasons[docId] || '';
    if (!reason.trim()) {
      showToast('error', 'Reason Required', 'Please provide a reason for rejecting this document.');
      return;
    }
    setDocStatuses(prev => ({ ...prev, [docId]: 'rejected' }));
    setShowRejectInput(null);
  };

  const handleDocRejectReasonChange = (docId: string, reason: string) => {
    setDocRejectReasons(prev => ({ ...prev, [docId]: reason }));
  };

  const handleVerify = async (staff: StaffProfileResponse, verified: boolean) => {
    // Build rejection notes from rejected documents
    let rejectionNotes = overallNotes;
    if (!verified && !rejectionNotes) {
      const rejectedDocs = docItems.filter(d => d.status === 'rejected');
      if (rejectedDocs.length > 0) {
        rejectionNotes = rejectedDocs
          .map(d => `${d.label}: ${d.rejectReason}`)
          .join('\n');
      }
    }

    try {
      await verifyMutation.mutateAsync({
        userId: staff.id,
        verified,
        rejectionReason: verified ? undefined : rejectionNotes || undefined,
      });

      // Send notification to staff
      try {
        if (verified) {
          await notificationService.create({
            userId: staff.id,
            title: 'Profile Verified!',
            message: 'Congratulations! Your profile has been verified. You can now accept staffing assignments.',
            type: 'approval',
            link: '/dashboard',
          });
        } else {
          await notificationService.create({
            userId: staff.id,
            title: 'Verification Rejected',
            message: `Your documents were rejected. Reason: ${rejectionNotes || 'Please review and resubmit.'}`,
            type: 'approval',
            link: '/complete-profile',
          });
        }
      } catch {
        // Notification is best-effort
      }

      showToast('success', verified ? 'Staff Verified' : 'Verification Rejected',
        `${staffDisplayName(staff)} has been ${verified ? 'approved' : 'rejected'}.`);
      setIsDetailModalOpen(false);
      setSelectedStaff(null);
      setOverallNotes('');
      setDocStatuses({});
      setDocRejectReasons({});
      setShowRejectInput(null);
    } catch {
      showToast('error', 'Action Failed', 'Could not update verification status.');
    }
  };

  const openReviewModal = (staff: StaffProfileResponse) => {
    // Reset doc statuses for fresh review
    setDocStatuses({});
    setDocRejectReasons({});
    setOverallNotes('');
    setShowRejectInput(null);
    setSelectedStaff(staff);
    setIsDetailModalOpen(true);
  };

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
        title="Staff Credential Verification"
        description="Review and verify professional licenses and identity documents for healthcare staff."
      />

      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between bg-slate-50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200 dark:border-slate-800">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search by staff name or email..."
            className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl py-2 pl-10 pr-4 text-sm text-slate-900 dark:text-white focus:ring-1 focus:ring-amber-500 outline-none"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-amber-500/5 border-amber-500/20">
          <CardContent className="p-4 flex items-center gap-4">
            <div className="p-3 rounded-2xl bg-amber-500/10 text-amber-500">
              <Clock className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Pending Verification</p>
              <p className="text-2xl font-black text-slate-900 dark:text-white">{pendingStaff.length}</p>
            </div>
          </CardContent>
        </Card>
        <Card className="bg-emerald-500/5 border-emerald-500/20">
          <CardContent className="p-4 flex items-center gap-4">
            <div className="p-3 rounded-2xl bg-emerald-500/10 text-emerald-500">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Verified</p>
              <p className="text-2xl font-black text-slate-900 dark:text-white">{verifiedStaff.length}</p>
            </div>
          </CardContent>
        </Card>
        <Card className="bg-slate-500/5 border-slate-500/20">
          <CardContent className="p-4 flex items-center gap-4">
            <div className="p-3 rounded-2xl bg-slate-500/10 text-slate-500">
              <Briefcase className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Staff</p>
              <p className="text-2xl font-black text-slate-900 dark:text-white">{allStaff.length}</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        {filteredStaff.length === 0 ? (
          <EmptyState
            title="No staff found"
            description="No staff members match your search criteria."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Staff Professional</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>License Validity</TableHead>
                <TableHead>Documents</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredStaff.map((staff) => (
                <TableRow key={staff.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-xs font-black text-amber-400">
                        {staff.firstName[0]}{staff.lastName[0]}
                      </div>
                      <div>
                        <p className="font-bold text-slate-900 dark:text-slate-100">{staffDisplayName(staff)}</p>
                        <p className="text-[10px] text-slate-500">{staff.email}</p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="primary" size="sm">{staff.staffCategory}</Badge>
                  </TableCell>
                  <TableCell>
                    <div className="space-y-1">
                      <span className="text-xs text-slate-600 dark:text-slate-400">
                        {staff.licenseValidityDate || '—'}
                      </span>
                      {staff.licenseStatus && staff.licenseStatus !== 'UNKNOWN' && (
                        <Badge
                          variant={
                            staff.licenseStatus === 'EXPIRED'
                              ? 'danger'
                              : staff.licenseStatus === 'EXPIRING_SOON'
                                ? 'warning'
                                : 'success'
                          }
                          size="sm"
                        >
                          {staff.licenseStatus === 'EXPIRED'
                            ? 'Expired'
                            : staff.licenseStatus === 'EXPIRING_SOON'
                              ? 'Expiring Soon'
                              : 'Valid'}
                        </Badge>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1.5">
                      {staff.stateBoardCertificatePath && (
                        <div title="License" className="w-6 h-6 rounded-lg flex items-center justify-center border bg-emerald-500/10 border-emerald-500/30 text-emerald-500">
                          <FileText className="w-3 h-3" />
                        </div>
                      )}
                      {(staff.educationalDocumentPaths || []).length > 0 && (
                        <div title="Other Docs" className="w-6 h-6 rounded-lg flex items-center justify-center border bg-emerald-500/10 border-emerald-500/30 text-emerald-500">
                          <FileText className="w-3 h-3" />
                        </div>
                      )}
                      {(staff.photoPaths || []).length > 0 && (
                        <div title="Photos" className="w-6 h-6 rounded-lg flex items-center justify-center border bg-emerald-500/10 border-emerald-500/30 text-emerald-500">
                          <FileText className="w-3 h-3" />
                        </div>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={staff.verified ? 'success' : 'warning'}
                      size="sm"
                    >
                      {staff.verified ? 'Verified' : 'Pending'}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => openReviewModal(staff)}
                      leftIcon={<Eye className="w-3.5 h-3.5" />}
                    >
                      Review
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>

      {/* ─── Review Modal ──────────────────────────────── */}
      {selectedStaff && (
        <Modal
          isOpen={isDetailModalOpen}
          onClose={() => { setIsDetailModalOpen(false); setSelectedStaff(null); }}
          title="Document Verification Review"
          maxWidth="2xl"
        >
          <div className="space-y-6">
            {/* Staff Header */}
            <div className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-2xl bg-amber-500/10 flex items-center justify-center text-amber-400 font-black">
                  {selectedStaff.firstName[0]}{selectedStaff.lastName[0]}
                </div>
                <div>
                  <h3 className="text-base font-black text-slate-900 dark:text-white">{staffDisplayName(selectedStaff)}</h3>
                  <p className="text-xs text-slate-400">{selectedStaff.staffCategory} &bull; {selectedStaff.email}</p>
                </div>
              </div>
              <Badge variant={selectedStaff.verified ? 'success' : 'warning'}>
                {selectedStaff.verified ? 'Verified' : 'Pending'}
              </Badge>
            </div>

            {/* Profile Quick Info */}
            <div className="grid grid-cols-3 gap-3">
              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 text-center">
                <p className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Phone</p>
                <p className="text-xs font-bold text-slate-900 dark:text-white mt-0.5">{selectedStaff.phone || '—'}</p>
              </div>
              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 text-center">
                <p className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">Aadhaar</p>
                <p className="text-xs font-bold text-slate-900 dark:text-white font-mono mt-0.5">{selectedStaff.aadharCardNumber || '—'}</p>
              </div>
              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 text-center">
                <p className="text-[10px] text-slate-400 uppercase font-bold tracking-wider">License Valid</p>
                <p className="text-xs font-bold text-slate-900 dark:text-white mt-0.5">{selectedStaff.licenseValidityDate || '—'}</p>
              </div>
            </div>

            {/* Per-Document Review */}
            <div className="space-y-3">
              <h4 className="text-xs font-black uppercase tracking-widest text-slate-500 flex items-center gap-2">
                <FileText className="w-3.5 h-3.5" />
                Document Review ({docItems.length} documents)
              </h4>

              {docItems.length === 0 ? (
                <div className="text-center py-8 text-slate-400">
                  <FileText className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <p className="text-xs">No documents uploaded yet.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {docItems.map((doc) => (
                    <DocumentReviewCard
                      key={doc.id}
                      doc={doc}
                      onApprove={handleDocApprove}
                      onReject={handleDocReject}
                      rejectReason={docRejectReasons[doc.id] || ''}
                      onRejectReasonChange={handleDocRejectReasonChange}
                      showRejectInput={showRejectInput}
                      setShowRejectInput={setShowRejectInput}
                    />
                  ))}
                </div>
              )}
            </div>

            {/* Previous Rejection Reason */}
            {!selectedStaff.verified && selectedStaff.rejectionReason && (
              <div className="flex items-start gap-3 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-800">
                <AlertCircle className="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                <div>
                  <p className="text-[10px] font-bold text-rose-600 uppercase tracking-wider">Previous Rejection Reason</p>
                  <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">{selectedStaff.rejectionReason}</p>
                </div>
              </div>
            )}

            {/* Overall Notes */}
            <div className="space-y-2">
              <label className="flex items-center gap-2 text-xs font-semibold text-slate-700 dark:text-slate-300">
                <MessageSquare className="w-3.5 h-3.5" />
                Admin Notes {hasRejected ? '(Required for rejection)' : '(Optional)'}
              </label>
              <textarea
                className="w-full rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm p-3 text-slate-900 dark:text-white focus:ring-1 focus:ring-amber-500 outline-none resize-none"
                rows={3}
                placeholder={hasRejected
                  ? 'Explain why documents were rejected...'
                  : 'Add any notes about this verification...'
                }
                value={overallNotes}
                onChange={(e) => setOverallNotes(e.target.value)}
              />
            </div>

            {/* Action Buttons */}
            <div className="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-800">
              <Button
                variant="ghost"
                onClick={() => { setIsDetailModalOpen(false); setSelectedStaff(null); }}
              >
                Close
              </Button>

              <div className="flex items-center gap-3">
                {selectedStaff.verified ? (
                  <Button
                    variant="danger"
                    onClick={() => handleVerify(selectedStaff, false)}
                    leftIcon={<XCircle className="w-4 h-4" />}
                    isLoading={verifyMutation.isPending}
                  >
                    Reject & Remove Verification
                  </Button>
                ) : (
                  <>
                    <Button
                      variant="ghost"
                      className="text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 border border-rose-200 dark:border-rose-800"
                      onClick={() => handleVerify(selectedStaff, false)}
                      leftIcon={<XCircle className="w-4 h-4" />}
                      isLoading={verifyMutation.isPending}
                      disabled={allApproved}
                    >
                      Reject All
                    </Button>
                    <Button
                      variant="primary"
                      className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black"
                      onClick={() => handleVerify(selectedStaff, true)}
                      leftIcon={<CheckCircle2 className="w-4 h-4" />}
                      isLoading={verifyMutation.isPending}
                    >
                      {allApproved ? 'Approve All & Verify' : 'Approve & Verify Staff'}
                    </Button>
                  </>
                )}
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
