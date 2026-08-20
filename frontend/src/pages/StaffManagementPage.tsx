import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { Avatar } from '../components/common/Avatar';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Drawer } from '../components/common/Drawer';
import { Tabs } from '../components/common/Tabs';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { staffService } from '../services/staffService';
import { StaffProfileResponse } from '../services/authService';
import { Search, Eye, Phone, Mail, ShieldCheck } from 'lucide-react';

/** Map backend staff profile to a display-friendly shape */
function staffDisplayName(s: StaffProfileResponse) {
  return `${s.firstName} ${s.lastName}`;
}

export const StaffManagementPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<string>('ALL');
  const [selectedStaff, setSelectedStaff] = useState<StaffProfileResponse | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['staff-management'],
    queryFn: () => staffService.getAllStaff(0, 100),
  });

  const staffList = data?.content || [];

  const filteredStaff = staffList.filter((staff) => {
    const fullName = staffDisplayName(staff);
    const matchesSearch =
      fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      staff.staffCategory.toLowerCase().includes(searchQuery.toLowerCase()) ||
      staff.email.toLowerCase().includes(searchQuery.toLowerCase());

    if (activeTab === 'ALL') return matchesSearch;
    if (activeTab === 'VERIFIED') return matchesSearch && staff.verified;
    if (activeTab === 'PENDING') return matchesSearch && !staff.verified;
    return matchesSearch;
  });

  const verifiedCount = staffList.filter((s) => s.verified).length;
  const pendingCount = staffList.filter((s) => !s.verified).length;

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
        title="Healthcare Staff Directory"
        description="Manage verified nurses, caregivers, technicians & physiotherapists across all partner hospital locations."
      />

      {/* Filter Bar */}
      <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        <Tabs
          tabs={[
            { id: 'ALL', label: 'All Staff', count: staffList.length },
            { id: 'VERIFIED', label: 'Verified', count: verifiedCount },
            { id: 'PENDING', label: 'Pending Verification', count: pendingCount },
          ]}
          activeTab={activeTab}
          onChange={setActiveTab}
          variant="pills"
        />

        <div className="w-full md:w-72">
          <Input
            placeholder="Search by name, category, or email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            leftIcon={<Search className="w-4 h-4 text-slate-400" />}
          />
        </div>
      </div>

      {/* Staff Table */}
      <Card>
        {filteredStaff.length === 0 ? (
          <EmptyState
            title="No staff found"
            description="No healthcare staff match your search criteria."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Staff Professional</TableHead>
                <TableHead>Category & Role</TableHead>
                <TableHead>License Info</TableHead>
                <TableHead>Aadhaar</TableHead>
                <TableHead>Verification</TableHead>
                <TableHead className="text-right">Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredStaff.map((staff) => {
                const fullName = staffDisplayName(staff);
                return (
                  <TableRow key={staff.id}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Avatar name={fullName} size="md" />
                        <div>
                          <p className="font-bold text-slate-900 dark:text-slate-100">{fullName}</p>
                          <p className="text-xs text-slate-500">{staff.email}</p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="primary" size="sm">{staff.staffCategory}</Badge>
                    </TableCell>
                    <TableCell>
                      {staff.licenseValidityDate ? (
                        <div>
                          <p className="text-xs font-medium">Valid until</p>
                          <p className="text-[11px] text-slate-500">{staff.licenseValidityDate}</p>
                        </div>
                      ) : (
                        <span className="text-xs text-slate-400">—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <span className="text-xs text-slate-600 dark:text-slate-400 font-mono">
                        {staff.aadharCardNumber || '—'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={staff.verified ? 'success' : 'warning'} size="sm" dot>
                        {staff.verified ? 'Verified' : 'Pending'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        leftIcon={<Eye className="w-3.5 h-3.5" />}
                        onClick={() => setSelectedStaff(staff)}
                      >
                        Details
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </Card>

      {/* Staff Detail Drawer */}
      {selectedStaff && (
        <Drawer
          isOpen={Boolean(selectedStaff)}
          onClose={() => setSelectedStaff(null)}
          title={`Healthcare Professional Profile - ${staffDisplayName(selectedStaff)}`}
          size="lg"
        >
          <div className="space-y-6 text-xs text-slate-700 dark:text-slate-300">
            <div className="flex items-center gap-4 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-800">
              <Avatar name={staffDisplayName(selectedStaff)} size="xl" />
              <div>
                <h3 className="text-base font-extrabold text-slate-900 dark:text-slate-100">
                  {staffDisplayName(selectedStaff)}
                </h3>
                <p className="text-xs font-bold text-teal-600 dark:text-teal-400">{selectedStaff.staffCategory}</p>
                <div className="flex items-center gap-2 mt-1">
                  <Badge variant={selectedStaff.verified ? 'success' : 'warning'} size="sm">
                    {selectedStaff.verified ? 'Verified' : 'Pending'}
                  </Badge>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="font-bold uppercase tracking-wider text-slate-500 text-[10px]">Contact Info</h4>
              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 space-y-1.5">
                <div className="flex items-center gap-2"><Mail className="w-3.5 h-3.5 text-slate-400" /> {selectedStaff.email}</div>
                <div className="flex items-center gap-2"><Phone className="w-3.5 h-3.5 text-slate-400" /> {selectedStaff.phone}</div>
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="font-bold uppercase tracking-wider text-slate-500 text-[10px]">License Details</h4>
              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Validity</span>
                  <span className="font-bold">{selectedStaff.licenseValidityDate || '—'}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Renewal Date</span>
                  <span className="font-bold">{selectedStaff.licenseRenewalDate || '—'}</span>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <h4 className="font-bold uppercase tracking-wider text-slate-500 text-[10px]">Uploaded Documents</h4>
              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-800 space-y-1.5">
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">State Board Certificate</span>
                  <Badge variant={selectedStaff.stateBoardCertificatePath ? 'success' : 'warning'} size="sm">
                    {selectedStaff.stateBoardCertificatePath ? 'Uploaded' : 'Not Uploaded'}
                  </Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Educational Documents</span>
                  <span className="font-bold">{selectedStaff.educationalDocumentPaths?.length || 0} files</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Photos</span>
                  <span className="font-bold">{selectedStaff.photoPaths?.length || 0} files</span>
                </div>
              </div>
            </div>

            {selectedStaff.verified && (
              <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900">
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                <span className="text-xs font-bold text-emerald-700 dark:text-emerald-400">
                  This staff member has been verified by the NurseAdda compliance team.
                </span>
              </div>
            )}
          </div>
        </Drawer>
      )}
    </div>
  );
};
