import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { Avatar } from '../components/common/Avatar';
import { Tabs } from '../components/common/Tabs';
import { Modal } from '../components/common/Modal';
import { Loader } from '../components/common/Loader';
import { staffService } from '../services/staffService';
import { StaffProfileResponse } from '../services/authService';
import { 
  Phone, 
  Mail, 
  MapPin, 
  ShieldCheck,
  FileText, 
  Eye, 
  ChevronLeft,
  Briefcase,
  Calendar,
  X
} from 'lucide-react';

function staffDisplayName(s: StaffProfileResponse) {
  return `${s.firstName} ${s.lastName}`;
}

export const StaffProfileDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('profile');
  const [viewingDoc, setViewingDoc] = useState<{ name: string; url: string } | null>(null);

  const { data: staff, isLoading, error } = useQuery({
    queryKey: ['staff-profile', id],
    queryFn: () => staffService.getStaffById(Number(id)),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-40">
        <Loader />
      </div>
    );
  }

  if (error || !staff) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <div className="w-16 h-16 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
          <Briefcase className="w-8 h-8 text-slate-400" />
        </div>
        <h2 className="text-xl font-black text-slate-900 dark:text-slate-100">Profile Not Found</h2>
        <p className="text-sm text-slate-500 mt-1">The professional profile you are looking for does not exist or has been removed.</p>
        <Button variant="outline" size="sm" className="mt-6" onClick={() => navigate(-1)}>
          Go Back
        </Button>
      </div>
    );
  }

  const fullName = staffDisplayName(staff);
  const allDocs = [
    ...(staff.stateBoardCertificatePath ? [{ name: 'State Board Certificate', type: 'PDF', url: `/api/uploads${staff.stateBoardCertificatePath}` }] : []),
    ...(staff.educationalDocumentPaths || []).map((p, i) => ({
      name: `Educational Document ${i + 1}`,
      type: 'PDF',
      url: `/api/uploads${p}`,
    })),
    ...(staff.photoPaths || []).map((p, i) => ({
      name: `Photo ${i + 1}`,
      type: 'JPG',
      url: `/api/uploads${p}`,
    })),
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" onClick={() => navigate(-1)} className="rounded-full w-9 h-9 p-0 flex items-center justify-center">
          <ChevronLeft className="w-5 h-5" />
        </Button>
        <PageHeader
          title="Professional Profile"
          description="Detailed verification documents and clinical credentials for your assigned healthcare professional."
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Essential Info */}
        <div className="lg:col-span-1 space-y-6">
          <Card>
            <CardContent className="p-6 text-center">
              <div className="relative inline-block mb-4">
                <Avatar name={fullName} size="xl" className="w-24 h-24 border-4 border-white dark:border-slate-900 shadow-xl" />
                {staff.verified && (
                  <div className="absolute bottom-1 right-1 w-6 h-6 bg-emerald-500 border-4 border-white dark:border-slate-900 rounded-full flex items-center justify-center">
                    <ShieldCheck className="w-3 h-3 text-white" />
                  </div>
                )}
              </div>
              
              <h2 className="text-xl font-black text-slate-900 dark:text-slate-100">{fullName}</h2>
              <p className="text-sm font-bold text-amber-600 dark:text-amber-400 mt-1">{staff.staffCategory}</p>
              
              <div className="flex items-center justify-center gap-3 mt-4">
                <Badge variant={staff.verified ? 'success' : 'warning'} size="sm">
                  {staff.verified ? 'Verified' : 'Pending'}
                </Badge>
              </div>

              <div className="mt-6 border-t border-slate-100 dark:border-slate-800 pt-6">
                <p className="text-[10px] uppercase font-bold text-slate-400 tracking-widest mb-1">Aadhaar</p>
                <p className="text-xs font-black text-slate-900 dark:text-slate-100 font-mono">{staff.aadharCardNumber || '—'}</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-xs uppercase tracking-widest text-slate-500">Contact Details</CardTitle>
            </CardHeader>
            <CardContent className="p-4 space-y-3">
              <div className="flex items-center gap-3 p-2.5 rounded-xl bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-800">
                <div className="w-8 h-8 rounded-lg bg-white dark:bg-slate-800 flex items-center justify-center shadow-xs">
                  <Mail className="w-4 h-4 text-amber-500" />
                </div>
                <div className="overflow-hidden">
                  <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Email Address</p>
                  <p className="text-xs font-semibold text-slate-900 dark:text-slate-100 truncate">{staff.email}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 p-2.5 rounded-xl bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-800">
                <div className="w-8 h-8 rounded-lg bg-white dark:bg-slate-800 flex items-center justify-center shadow-xs">
                  <Phone className="w-4 h-4 text-emerald-500" />
                </div>
                <div>
                  <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Phone Number</p>
                  <p className="text-xs font-semibold text-slate-900 dark:text-slate-100">{staff.phone}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Detailed Tabs */}
        <div className="lg:col-span-2 space-y-6">
          <Tabs
            tabs={[
              { id: 'profile', label: 'License & Credentials' },
              { id: 'documents', label: 'Verified Documents', count: allDocs.length }
            ]}
            activeTab={activeTab}
            onChange={setActiveTab}
            variant="underline"
          />

          {activeTab === 'profile' && (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm font-black flex items-center gap-2">
                    <ShieldCheck className="w-4 h-4 text-emerald-500" />
                    License Details
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="p-4 rounded-2xl bg-amber-50/50 dark:bg-amber-900/10 border border-amber-100/50 dark:border-amber-900/30">
                    <p className="text-[10px] uppercase font-bold text-amber-600 dark:text-amber-400 tracking-widest mb-1">Staff Category</p>
                    <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{staff.staffCategory}</p>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-4 rounded-xl border border-slate-100 dark:border-slate-800">
                      <p className="text-[10px] uppercase font-bold text-slate-400 tracking-widest mb-1">License Validity</p>
                      <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{staff.licenseValidityDate || '—'}</p>
                    </div>
                    <div className="p-4 rounded-xl border border-slate-100 dark:border-slate-800">
                      <p className="text-[10px] uppercase font-bold text-slate-400 tracking-widest mb-1">License Renewal</p>
                      <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{staff.licenseRenewalDate || '—'}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {staff.verified && (
                <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900">
                  <ShieldCheck className="w-4 h-4 text-emerald-500" />
                  <span className="text-xs font-bold text-emerald-700 dark:text-emerald-400">
                    This staff member has been verified by the NurseAdda compliance team.
                  </span>
                </div>
              )}
            </div>
          )}

          {activeTab === 'documents' && (
            <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
              <div className="bg-amber-50 dark:bg-amber-950/20 border border-amber-100 dark:border-amber-900/40 p-4 rounded-2xl flex items-start gap-3">
                <ShieldCheck className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-bold text-amber-900 dark:text-amber-100">Verification Seal</p>
                  <p className="text-xs text-amber-700 dark:text-amber-400/80 mt-1">
                    All documents below have been uploaded by the staff member. Verification status is managed by the NurseAdda compliance team.
                  </p>
                </div>
              </div>

              {allDocs.length === 0 ? (
                <Card>
                  <CardContent className="p-8 text-center">
                    <FileText className="w-8 h-8 text-slate-300 mx-auto mb-3" />
                    <p className="text-sm font-bold text-slate-500">No documents uploaded</p>
                    <p className="text-xs text-slate-400 mt-1">This staff member hasn't uploaded any documents yet.</p>
                  </CardContent>
                </Card>
              ) : (
                <Card>
                  <CardContent className="p-0">
                    <div className="divide-y divide-slate-100 dark:divide-slate-800">
                      {allDocs.map((doc, idx) => (
                        <div key={idx} className="p-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors group">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-500 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
                              <FileText className="w-5 h-5" />
                            </div>
                            <div>
                              <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{doc.name}</p>
                              <p className="text-[10px] text-slate-500">{doc.type}</p>
                            </div>
                          </div>
                          <Button 
                            variant="ghost" 
                            size="sm" 
                            className="h-8 w-8 p-0 text-slate-400 hover:text-amber-500"
                            onClick={() => setViewingDoc({ name: doc.name, url: doc.url })}
                          >
                            <Eye className="w-4 h-4" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Document Preview Modal */}
      <Modal
        isOpen={!!viewingDoc}
        onClose={() => setViewingDoc(null)}
        title={viewingDoc?.name || 'Document Preview'}
        maxWidth="lg"
      >
        <div className="p-1 space-y-4">
          <div className="relative aspect-[4/3] rounded-2xl overflow-hidden bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 flex items-center justify-center">
            {viewingDoc && (
              <img 
                src={viewingDoc.url} 
                alt={viewingDoc.name} 
                className="w-full h-full object-contain"
                referrerPolicy="no-referrer"
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = 'none';
                }}
              />
            )}
            {viewingDoc && (
              <div className="absolute top-4 right-4 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md px-3 py-1.5 rounded-full border border-slate-200 dark:border-slate-700 shadow-sm flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                <span className="text-[10px] font-bold text-slate-600 dark:text-slate-300 tracking-wider uppercase">Staff Upload</span>
              </div>
            )}
          </div>
          
          <div className="flex items-center justify-end gap-2">
            <Button variant="secondary" size="sm" onClick={() => setViewingDoc(null)}>
              Close Viewer
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
