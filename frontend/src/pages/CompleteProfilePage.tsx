import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useToast } from '../components/common/Toast';
import { useAuthStore } from '../store/useAuthStore';
import { authService } from '../services/authService';
import { staffService } from '../services/staffService';
import { Button } from '../components/common/Button';
import { Card, CardContent, CardHeader, CardTitle } from '../components/common/Card';
import {
  Upload,
  FileText,
  ShieldCheck,
  AlertCircle,
  FileBadge,
  CreditCard,
  UserCheck,
  Loader2,
  GraduationCap,
  Plus,
  RefreshCw,
} from 'lucide-react';
import { Input } from '../components/common/Input';

interface DocumentType {
  id: string;
  name: string;
  description: string;
  icon: React.ElementType;
  required: boolean;
  field: 'stateBoardCertificate' | 'educationalDocuments' | 'photos';
}

const REQUIRED_DOCUMENTS: DocumentType[] = [
  {
    id: 'license',
    name: 'Professional License',
    description: 'Current nursing or technician license issued by state board.',
    icon: FileBadge,
    required: true,
    field: 'stateBoardCertificate',
  },
  {
    id: 'id_proof',
    name: 'Identity Proof',
    description: 'Government issued ID (Passport, Driving License, or Aadhar).',
    icon: CreditCard,
    required: true,
    field: 'photos',
  },
  {
    id: 'background_check',
    name: 'Police Clearance',
    description: 'Recent background check or police clearance certificate.',
    icon: ShieldCheck,
    required: true,
    field: 'educationalDocuments',
  },
  {
    id: 'experience',
    name: 'Experience Letter',
    description: 'Relieving letter or experience certificate from last employer.',
    icon: UserCheck,
    required: false,
    field: 'educationalDocuments',
  },
];

export const CompleteProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { user } = useAuthStore();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadedDocs, setUploadedDocs] = useState<Record<string, File | null>>({});
  const [uploadStatus, setUploadStatus] = useState<Record<string, 'idle' | 'uploading' | 'success'>>({});

  // Fetch existing staff profile to pre-fill data (for re-submission after rejection)
  const { data: existingProfile } = useQuery({
    queryKey: ['my-staff-profile'],
    queryFn: () => staffService.getMyProfile(),
    retry: false,
  });

  const [profileData, setProfileData] = useState({
    aadharCardNumber: '',
    location: '',
    licenseValidityDate: '',
    licenseRenewalDate: '',
  });

  // Pre-fill form when existing profile loads
  useEffect(() => {
    if (existingProfile) {
      setProfileData({
        aadharCardNumber: existingProfile.aadharCardNumber || '',
        location: existingProfile.location || '',
        licenseValidityDate: existingProfile.licenseValidityDate || '',
        licenseRenewalDate: existingProfile.licenseRenewalDate || '',
      });
    }
  }, [existingProfile]);

  const isResubmission = existingProfile && existingProfile.verified === false;

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setProfileData((prev) => ({ ...prev, [name]: value }));
  };

  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const handleFileChange = (docId: string, file: File | null) => {
    if (!file) return;

    // Validate file size
    if (file.size > MAX_FILE_SIZE) {
      const docName = REQUIRED_DOCUMENTS.find(d => d.id === docId)?.name || docId;
      showToast(
        'error',
        'File Too Large',
        `${docName}: "${file.name}" is ${formatFileSize(file.size)}. Maximum allowed size is 10 MB.`,
      );
      return;
    }

    setUploadStatus((prev) => ({ ...prev, [docId]: 'uploading' }));

    // Simulate brief upload delay for UX
    setTimeout(() => {
      setUploadedDocs((prev) => ({ ...prev, [docId]: file }));
      setUploadStatus((prev) => ({ ...prev, [docId]: 'success' }));
      showToast('success', 'File Selected', `${file.name} (${formatFileSize(file.size)}) ready for upload.`);
    }, 600);
  };

  const handleSubmit = async () => {
    const missingRequired = REQUIRED_DOCUMENTS.filter((doc) => doc.required && !uploadedDocs[doc.id]);

    if (missingRequired.length > 0) {
      showToast('error', 'Missing Documents', `Please upload: ${missingRequired.map((d) => d.name).join(', ')}`);
      return;
    }

    setIsSubmitting(true);
    try {
      // Collect files by field
      let stateBoardCertificate: File | undefined;
      const educationalDocuments: File[] = [];
      const photos: File[] = [];

      for (const doc of REQUIRED_DOCUMENTS) {
        const file = uploadedDocs[doc.id];
        if (!file) continue;
        if (doc.field === 'stateBoardCertificate') {
          stateBoardCertificate = file;
        } else if (doc.field === 'educationalDocuments') {
          educationalDocuments.push(file);
        } else if (doc.field === 'photos') {
          photos.push(file);
        }
      }

      await authService.updateStaffProfile({
        aadharCardNumber: profileData.aadharCardNumber || undefined,
        location: profileData.location || undefined,
        licenseValidityDate: profileData.licenseValidityDate || undefined,
        licenseRenewalDate: profileData.licenseRenewalDate || undefined,
        stateBoardCertificate,
        educationalDocuments: educationalDocuments.length > 0 ? educationalDocuments : undefined,
        photos: photos.length > 0 ? photos : undefined,
      });

      useAuthStore.getState().completeProfile();
      showToast('success', 'Profile Submitted', 'Documents sent for verification. You will be notified once verified.');
      navigate('/dashboard');
    } catch (err: unknown) {
      // Extract specific error message from backend response
      let errorMsg = 'Something went wrong. Please try again.';
      const axiosErr = err as { response?: { data?: { message?: string; error?: string } }; message?: string };
      if (axiosErr?.response?.data?.message) {
        errorMsg = axiosErr.response.data.message;
      } else if (axiosErr?.response?.data?.error) {
        errorMsg = axiosErr.response.data.error;
      } else if (axiosErr?.message?.includes('max upload size')) {
        errorMsg = 'One or more files exceed the 10 MB size limit. Please use smaller files.';
      } else if (axiosErr?.message?.includes('Request Entity Too Large')) {
        errorMsg = 'Total file size exceeds the server limit. Please reduce file sizes.';
      }
      showToast('error', 'Submission Failed', errorMsg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 py-4 px-4">
      <div className="text-center space-y-2">
        {isResubmission ? (
          <>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-950 text-amber-300 border border-amber-800 text-[10px] font-bold uppercase tracking-wider mb-2">
              <RefreshCw className="w-3 h-3" />
              Re-submission: Update Documents
            </div>
            <h1 className="text-3xl font-black text-white tracking-tight">Update Your Profile</h1>
            <p className="text-slate-400 max-w-lg mx-auto">
              Your previous submission needs updates. Please re-upload the required documents.
            </p>
          </>
        ) : (
          <>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-950 text-amber-300 border border-amber-800 text-[10px] font-bold uppercase tracking-wider mb-2">
              Step 2: Professional Verification
            </div>
            <h1 className="text-3xl font-black text-white tracking-tight">Complete Your Profile</h1>
            <p className="text-slate-400 max-w-lg mx-auto">
              Provide your professional background and upload credentials to start receiving shift assignments.
            </p>
          </>
        )}
      </div>

      {/* Rejection notice */}
      {isResubmission && (
        <div className="flex items-start gap-3 p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30">
          <AlertCircle className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
          <div className="text-xs text-amber-200/80 leading-relaxed">
            <p className="font-bold text-amber-400 mb-1">Previous Submission Needs Updates</p>
            Your documents were reviewed and require changes. Please update the highlighted fields and re-submit for verification.
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Side: Professional Info */}
        <div className="lg:col-span-1 space-y-6">
          <Card className="border-slate-800 bg-slate-900/50">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm flex items-center gap-2">
                <GraduationCap className="w-4 h-4 text-purple-400" />
                Professional Details
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Input
                label="Aadhar Card Number"
                name="aadharCardNumber"
                value={profileData.aadharCardNumber}
                onChange={handleProfileChange}
                placeholder="12-digit Aadhar number"
                className="bg-slate-800 border-slate-700 text-xs"
                maxLength={12}
              />
              <Input
                label="Location"
                name="location"
                value={profileData.location}
                onChange={handleProfileChange}
                placeholder="e.g. Mumbai, Maharashtra"
                className="bg-slate-800 border-slate-700 text-xs"
              />
              <Input
                label="License Validity Date"
                name="licenseValidityDate"
                type="date"
                value={profileData.licenseValidityDate}
                onChange={handleProfileChange}
                className="bg-slate-800 border-slate-700 text-xs"
              />
              <Input
                label="License Renewal Date"
                name="licenseRenewalDate"
                type="date"
                value={profileData.licenseRenewalDate}
                onChange={handleProfileChange}
                className="bg-slate-800 border-slate-700 text-xs"
              />
            </CardContent>
          </Card>
        </div>

        {/* Right Side: Document Uploads */}
        <div className="lg:col-span-2 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {REQUIRED_DOCUMENTS.map((doc) => (
              <Card key={doc.id} className="relative overflow-hidden border-slate-800 bg-slate-900/50 hover:bg-slate-900 transition-colors group">
                <CardContent className="p-5">
                  <div className="flex items-start gap-3">
                    <div className={`p-2 rounded-xl ${uploadedDocs[doc.id] ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-400'}`}>
                      <doc.icon className="w-5 h-5" />
                    </div>
                    <div className="flex-1 space-y-0.5">
                      <div className="flex items-center gap-2">
                        <h3 className="text-xs font-bold text-white">{doc.name}</h3>
                        {doc.required && (
                          <span className="text-[9px] font-bold text-rose-400 uppercase">Req</span>
                        )}
                      </div>
                      <p className="text-[10px] text-slate-500 leading-tight">{doc.description}</p>
                    </div>
                  </div>

                  <div className="mt-4">
                    <p className="text-[9px] text-slate-600 mb-2">Max 10 MB • PDF, JPG, PNG</p>
                    {uploadStatus[doc.id] === 'success' ? (
                      <div className="flex items-center justify-between p-2 rounded-lg bg-emerald-500/5 border border-emerald-500/20 text-emerald-400">
                        <div className="flex items-center gap-2 text-[10px] font-semibold truncate pr-2">
                          <FileText className="w-3.5 h-3.5" />
                          <span className="truncate">{uploadedDocs[doc.id]?.name}</span>
                        </div>
                        <button
                          onClick={() => {
                            setUploadedDocs((prev) => ({ ...prev, [doc.id]: null }));
                            setUploadStatus((prev) => ({ ...prev, [doc.id]: 'idle' }));
                          }}
                          className="text-[9px] font-bold uppercase hover:underline text-emerald-500 shrink-0"
                        >
                          Change
                        </button>
                      </div>
                    ) : (
                      <label className="cursor-pointer group/upload">
                        <input
                          type="file"
                          className="hidden"
                          onChange={(e) => handleFileChange(doc.id, e.target.files?.[0] || null)}
                          accept=".pdf,.jpg,.jpeg,.png"
                          disabled={uploadStatus[doc.id] === 'uploading'}
                        />
                        <div
                          className={`flex flex-col items-center justify-center p-4 border-2 border-dashed rounded-xl transition-all ${
                            uploadStatus[doc.id] === 'uploading'
                              ? 'border-amber-500/50 bg-amber-500/5'
                              : 'border-slate-800 hover:border-amber-500/40 hover:bg-amber-500/5'
                          }`}
                        >
                          {uploadStatus[doc.id] === 'uploading' ? (
                            <Loader2 className="w-5 h-5 text-amber-500 animate-spin" />
                          ) : (
                            <div className="flex items-center gap-2">
                              <Plus className="w-4 h-4 text-slate-500 group-hover/upload:text-amber-400" />
                              <span className="text-[10px] font-bold text-slate-500 group-hover/upload:text-amber-400 uppercase tracking-wider">
                                Upload
                              </span>
                            </div>
                          )}
                        </div>
                      </label>
                    )}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </div>

      <div className="flex flex-col items-center gap-6 pt-6">
        <div className="flex items-start gap-3 p-4 rounded-2xl bg-amber-500/5 border border-amber-500/20 max-w-xl">
          <AlertCircle className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
          <div className="text-xs text-amber-200/80 leading-relaxed">
            <p className="font-bold text-amber-500 mb-1">Verification Disclaimer</p>
            By submitting these documents, you authorize NurseAdda to perform background checks and verify your professional status with relevant authorities.
          </div>
        </div>

        <div className="flex items-center gap-4 w-full sm:w-auto">
          <Button
            variant="ghost"
            className="text-slate-400 hover:text-white"
            onClick={() => navigate('/dashboard')}
          >
            {isResubmission ? 'Back to Dashboard' : 'Skip for now'}
          </Button>
          <Button
            variant="primary"
            size="lg"
            className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-black min-w-[200px]"
            onClick={handleSubmit}
            isLoading={isSubmitting}
            disabled={Object.values(uploadStatus).filter((s) => s === 'success').length === 0}
            leftIcon={isResubmission ? <RefreshCw className="w-4 h-4" /> : undefined}
          >
            {isResubmission ? 'Re-submit for Verification' : 'Submit for Verification'}
          </Button>
        </div>
      </div>
    </div>
  );
};
