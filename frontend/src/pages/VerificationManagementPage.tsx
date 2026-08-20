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
  Phone
} from 'lucide-react';

function staffDisplayName(s: StaffProfileResponse) {
  return `${s.firstName} ${s.lastName}`;
}

export const VerificationManagementPage: React.FC = () => {
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStaff, setSelectedStaff] = useState<StaffProfileResponse | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ['staff-verification'],
    queryFn: () => staffService.getAllStaff(0, 100),
  });

  const verifyMutation = useMutation({
    mutationFn: ({ userId, verified }: { userId: number; verified: boolean }) =>
      staffService.verifyStaff(userId, verified),
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

  const handleVerify = async (staff: StaffProfileResponse, verified: boolean) => {
    try {
      await verifyMutation.mutateAsync({ userId: staff.id, verified });
      showToast('success', verified ? 'Staff Verified' : 'Verification Removed',
        `${staffDisplayName(staff)} has been ${verified ? 'verified' : 'unverified'}.`);
      setIsDetailModalOpen(false);
      setSelectedStaff(null);
    } catch {
      showToast('error', 'Action Failed', 'Could not update verification status.');
    }
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
            className="w-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl py-2 pl-10 pr-4 text-sm text-slate-900 dark:text-white focus:ring-1 focus:ring-sky-500 outline-none"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-sky-500/5 border-sky-500/20">
          <CardContent className="p-4 flex items-center gap-4">
            <div className="p-3 rounded-2xl bg-sky-500/10 text-sky-500">
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
                      <div className="w-9 h-9 rounded-xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-xs font-black text-sky-400">
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
                    <span className="text-xs text-slate-600 dark:text-slate-400">
                      {staff.licenseValidityDate || '—'}
                    </span>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1.5">
                      {staff.stateBoardCertificatePath && (
                        <div title="State Board Certificate" className="w-6 h-6 rounded-lg flex items-center justify-center border bg-emerald-500/10 border-emerald-500/30 text-emerald-500">
                          <FileText className="w-3 h-3" />
                        </div>
                      )}
                      {(staff.educationalDocumentPaths || []).length > 0 && (
                        <div title="Educational Documents" className="w-6 h-6 rounded-lg flex items-center justify-center border bg-emerald-500/10 border-emerald-500/30 text-emerald-500">
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
                      onClick={() => { setSelectedStaff(staff); setIsDetailModalOpen(true); }}
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

      {/* Detail Modal */}
      {selectedStaff && (
        <Modal
          isOpen={isDetailModalOpen}
          onClose={() => { setIsDetailModalOpen(false); setSelectedStaff(null); }}
          title="Profile Verification Review"
          maxWidth="lg"
        >
          <div className="space-y-6">
            <div className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-2xl bg-sky-500/10 flex items-center justify-center text-sky-400 font-black">
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

            <div className="space-y-4">
              <h4 className="text-xs font-black uppercase tracking-widest text-slate-500 flex items-center gap-2">
                <FileText className="w-3.5 h-3.5" />
                Profile Details
              </h4>
              
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2">
                  <div className="flex items-center gap-2 text-slate-500">
                    <Phone className="w-3.5 h-3.5" /> <span className="text-xs font-medium">Phone</span>
                  </div>
                  <p className="text-sm font-bold text-slate-900 dark:text-white">{selectedStaff.phone}</p>
                </div>
                <div className="p-4 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2">
                  <div className="flex items-center gap-2 text-slate-500">
                    <Mail className="w-3.5 h-3.5" /> <span className="text-xs font-medium">Email</span>
                  </div>
                  <p className="text-sm font-bold text-slate-900 dark:text-white">{selectedStaff.email}</p>
                </div>
              </div>

              <div className="p-4 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2">
                <div className="flex items-center gap-2 text-slate-500">
                  <ShieldCheck className="w-3.5 h-3.5" /> <span className="text-xs font-medium">Aadhaar Number</span>
                </div>
                <p className="text-sm font-bold text-slate-900 dark:text-white font-mono">{selectedStaff.aadharCardNumber || 'Not provided'}</p>
              </div>

              <div className="p-4 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-2">
                <div className="flex items-center gap-2 text-slate-500">
                  <ShieldCheck className="w-3.5 h-3.5" /> <span className="text-xs font-medium">License</span>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-[10px] text-slate-400 uppercase tracking-wider">Validity</p>
                    <p className="text-sm font-bold text-slate-900 dark:text-white">{selectedStaff.licenseValidityDate || '—'}</p>
                  </div>
                  <div>
                    <p className="text-[10px] text-slate-400 uppercase tracking-wider">Renewal</p>
                    <p className="text-sm font-bold text-slate-900 dark:text-white">{selectedStaff.licenseRenewalDate || '—'}</p>
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <h4 className="text-xs font-black uppercase tracking-widest text-slate-500 flex items-center gap-2">
                  <FileText className="w-3.5 h-3.5" />
                  Uploaded Documents
                </h4>
                <div className="grid grid-cols-1 gap-2">
                  {selectedStaff.stateBoardCertificatePath && (
                    <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <FileText className="w-4 h-4 text-slate-400" />
                        <span className="text-sm font-bold text-slate-900 dark:text-white">State Board Certificate</span>
                      </div>
                      <Badge variant="success" size="sm">Uploaded</Badge>
                    </div>
                  )}
                  {(selectedStaff.educationalDocumentPaths || []).length > 0 && (
                    <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <FileText className="w-4 h-4 text-slate-400" />
                        <span className="text-sm font-bold text-slate-900 dark:text-white">
                          Educational Documents ({selectedStaff.educationalDocumentPaths.length})
                        </span>
                      </div>
                      <Badge variant="success" size="sm">Uploaded</Badge>
                    </div>
                  )}
                  {(selectedStaff.photoPaths || []).length > 0 && (
                    <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <FileText className="w-4 h-4 text-slate-400" />
                        <span className="text-sm font-bold text-slate-900 dark:text-white">
                          Photos ({selectedStaff.photoPaths.length})
                        </span>
                      </div>
                      <Badge variant="success" size="sm">Uploaded</Badge>
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-800">
              <Button variant="ghost" onClick={() => { setIsDetailModalOpen(false); setSelectedStaff(null); }}>
                Close
              </Button>
              {selectedStaff.verified ? (
                <Button 
                  variant="danger" 
                  onClick={() => handleVerify(selectedStaff, false)}
                  leftIcon={<XCircle className="w-4 h-4" />}
                  isLoading={verifyMutation.isPending}
                >
                  Remove Verification
                </Button>
              ) : (
                <Button 
                  variant="primary" 
                  className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black"
                  onClick={() => handleVerify(selectedStaff, true)}
                  leftIcon={<CheckCircle2 className="w-4 h-4" />}
                  isLoading={verifyMutation.isPending}
                >
                  Approve & Verify Staff
                </Button>
              )}
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
