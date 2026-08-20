import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { useToast } from '../components/common/Toast';
import { Button } from '../components/common/Button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/common/Card';
import { 
  Upload, 
  CheckCircle2, 
  FileText, 
  ShieldCheck, 
  AlertCircle,
  FileBadge,
  CreditCard,
  UserCheck,
  Loader2,
  GraduationCap,
  Wand2,
  ScrollText,
  Plus
} from 'lucide-react';
import { Input } from '../components/common/Input';

interface DocumentType {
  id: string;
  name: string;
  description: string;
  icon: React.ElementType;
  required: boolean;
}

const REQUIRED_DOCUMENTS: DocumentType[] = [
  {
    id: 'license',
    name: 'Professional License',
    description: 'Current nursing or technician license issued by state board.',
    icon: FileBadge,
    required: true
  },
  {
    id: 'id_proof',
    name: 'Identity Proof',
    description: 'Government issued ID (Passport, Driving License, or Aadhar).',
    icon: CreditCard,
    required: true
  },
  {
    id: 'background_check',
    name: 'Police Clearance',
    description: 'Recent background check or police clearance certificate.',
    icon: ShieldCheck,
    required: true
  },
  {
    id: 'experience',
    name: 'Experience Letter',
    description: 'Relieving letter or experience certificate from last employer.',
    icon: UserCheck,
    required: false
  }
];

export const CompleteProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { completeProfile } = useAuthStore();
  const { showToast } = useToast();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadedDocs, setUploadedDocs] = useState<Record<string, File | null>>({});
  const [uploadStatus, setUploadStatus] = useState<Record<string, 'idle' | 'uploading' | 'success'>>({});
  
  const [profileData, setProfileData] = useState({
    summary: '',
    education: '',
    skills: '',
    certifications: ''
  });

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setProfileData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (docId: string, file: File | null) => {
    if (!file) return;

    setUploadStatus(prev => ({ ...prev, [docId]: 'uploading' }));
    
    // Simulate upload delay
    setTimeout(() => {
      setUploadedDocs(prev => ({ ...prev, [docId]: file }));
      setUploadStatus(prev => ({ ...prev, [docId]: 'success' }));
      showToast('success', 'File Uploaded', `${file.name} uploaded successfully.`);
    }, 1200);
  };

  const handleSubmit = async () => {
    const missingRequired = REQUIRED_DOCUMENTS.filter(doc => doc.required && !uploadedDocs[doc.id]);
    
    if (missingRequired.length > 0) {
      showToast('error', 'Missing Documents', `Please upload: ${missingRequired.map(d => d.name).join(', ')}`);
      return;
    }

    setIsSubmitting(true);
    try {
      // Simulate API call to update profile status
      await new Promise(resolve => setTimeout(resolve, 2000));
      completeProfile();
      showToast('success', 'Profile Submitted', 'Documents sent for verification. You will be notified once verified.');
      navigate('/dashboard');
    } catch {
      showToast('error', 'Submission Failed', 'Something went wrong. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 py-4 px-4">
      <div className="text-center space-y-2">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-sky-950 text-sky-300 border border-sky-800 text-[10px] font-bold uppercase tracking-wider mb-2">
          Step 2: Professional Verification
        </div>
        <h1 className="text-3xl font-black text-white tracking-tight">Complete Your Profile</h1>
        <p className="text-slate-400 max-w-lg mx-auto">
          Provide your professional background and upload credentials to start receiving shift assignments.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Side: Professional Info */}
        <div className="lg:col-span-1 space-y-6">
          <Card className="border-slate-800 bg-slate-900/50">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm flex items-center gap-2">
                <ScrollText className="w-4 h-4 text-sky-400" />
                Professional Summary
              </CardTitle>
            </CardHeader>
            <CardContent>
              <textarea
                name="summary"
                value={profileData.summary}
                onChange={handleProfileChange}
                placeholder="Briefly describe your nursing experience, specializations, and patient care philosophy..."
                className="w-full h-32 bg-slate-800 border border-slate-700 rounded-xl p-3 text-xs text-white focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all outline-none resize-none"
              />
            </CardContent>
          </Card>

          <Card className="border-slate-800 bg-slate-900/50">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm flex items-center gap-2">
                <GraduationCap className="w-4 h-4 text-purple-400" />
                Education & Skills
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Input
                label="Highest Degree"
                name="education"
                value={profileData.education}
                onChange={handleProfileChange}
                placeholder="e.g. B.Sc Nursing"
                className="bg-slate-800 border-slate-700 text-xs"
              />
              <Input
                label="Core Skills (Comma separated)"
                name="skills"
                value={profileData.skills}
                onChange={handleProfileChange}
                placeholder="e.g. ICU, Trauma, BLS"
                className="bg-slate-800 border-slate-700 text-xs"
                leftIcon={<Wand2 className="w-3.5 h-3.5 text-sky-400" />}
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
                    <div className={`p-2 rounded-xl ${uploadedDocs[doc.id] ? 'bg-emerald-500/10 text-emerald-400' : 'bg-sky-500/10 text-sky-400'}`}>
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
                    {uploadStatus[doc.id] === 'success' ? (
                      <div className="flex items-center justify-between p-2 rounded-lg bg-emerald-500/5 border border-emerald-500/20 text-emerald-400">
                        <div className="flex items-center gap-2 text-[10px] font-semibold truncate pr-2">
                          <FileText className="w-3.5 h-3.5" />
                          <span className="truncate">{uploadedDocs[doc.id]?.name}</span>
                        </div>
                        <button 
                          onClick={() => {
                            setUploadedDocs(prev => ({ ...prev, [doc.id]: null }));
                            setUploadStatus(prev => ({ ...prev, [doc.id]: 'idle' }));
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
                        <div className={`flex flex-col items-center justify-center p-4 border-2 border-dashed rounded-xl transition-all ${
                          uploadStatus[doc.id] === 'uploading' 
                            ? 'border-sky-500/50 bg-sky-500/5' 
                            : 'border-slate-800 hover:border-sky-500/40 hover:bg-sky-500/5'
                        }`}>
                          {uploadStatus[doc.id] === 'uploading' ? (
                            <Loader2 className="w-5 h-5 text-sky-500 animate-spin" />
                          ) : (
                            <div className="flex items-center gap-2">
                              <Plus className="w-4 h-4 text-slate-500 group-hover/upload:text-sky-400" />
                              <span className="text-[10px] font-bold text-slate-500 group-hover/upload:text-sky-400 uppercase tracking-wider">
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
            Skip for now
          </Button>
          <Button 
            variant="primary" 
            size="lg"
            className="bg-sky-500 hover:bg-sky-400 text-slate-950 font-black min-w-[200px]"
            onClick={handleSubmit}
            isLoading={isSubmitting}
            disabled={Object.values(uploadStatus).filter(s => s === 'success').length === 0}
          >
            Submit for Verification
          </Button>
        </div>
      </div>
    </div>
  );
};
