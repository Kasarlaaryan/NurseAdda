export type UserRole = 'ROLE_SUPER_ADMIN' | 'ROLE_ADMIN' | 'ROLE_STAFF' | 'ROLE_USER';

export type HealthcareRole = 
  | 'Registered Nurse (RN)'
  | 'Nurse Practitioner (NP)'
  | 'Clinical Nurse Specialist (CNS)'
  | 'Certified Nurse Midwife (CNM)'
  | 'Licensed Practical Nurse (LPN)'
  | 'General Nursing and Midwifery (GNM)'
  | 'Auxiliary Nurse Midwifery (ANM)'
  | 'Physiotherapist'
  | 'Medical Lab Technician';

export type OrganizationType = 'Hospital' | 'Clinic' | 'Corporate Healthcare Organization';

export type StaffStatus = 'Active' | 'Pending Verification' | 'On Assignment' | 'On Leave' | 'Inactive';

export type RequestStatus = 'Open' | 'Matching' | 'Partially Filled' | 'Fulfilled' | 'Cancelled';

export type AssignmentStatus = 'Pending' | 'Accepted' | 'In Progress' | 'Completed' | 'Cancelled';

export type AttendanceStatus = 'Checked In' | 'Checked Out' | 'Late' | 'Absent' | 'Overtime';

export type InvoiceStatus = 'Draft' | 'Sent' | 'Paid' | 'Overdue' | 'Partially Paid';

export type PaymentStatus = 'Pending' | 'Processing' | 'Completed' | 'Failed' | 'Refunded';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  avatarUrl?: string;
  phone?: string;
  department?: string;
  organizationName?: string;
  staffCategory?: HealthcareRole;
  lastActive?: string;
  isProfileComplete?: boolean;
}

export interface HealthcareStaff {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  category: HealthcareRole;
  status: StaffStatus;
  skills: string[];
  experienceYears: number;
  rating: number;
  completedShifts: number;
  hourlyRate: number;
  location: string;
  verificationStatus: 'Verified' | 'Pending' | 'Rejected';
  avatarUrl?: string;
  certifications: { id: string; name: string; expiryDate: string; isVerified: boolean }[];
  education: string;
  availability: 'Immediate' | 'Weekdays' | 'Night Shifts' | 'Weekends' | 'Unavailable';
  currentAssignment?: string;
  documentsCount: number;
  joinedDate: string;
}

export interface ClientOrganization {
  id: string;
  name: string;
  type: OrganizationType;
  email: string;
  phone: string;
  address: string;
  city: string;
  branchesCount: number;
  activeRequestsCount: number;
  activeStaffCount: number;
  primaryContactName: string;
  primaryContactRole: string;
  contractStatus: 'Active' | 'Pending Renewal' | 'Suspended';
  logoUrl?: string;
  totalBilled: number;
  outstandingBalance: number;
}

export interface StaffingRequest {
  id: string;
  requestNumber: string;
  clientOrgId: string;
  clientOrgName: string;
  requiredRole: HealthcareRole;
  quantityNeeded: number;
  fulfilledQuantity: number;
  shiftType: 'Day Shift' | 'Night Shift' | '24hr On-Call' | 'Rotational';
  startDate: string;
  endDate: string;
  hourlyPayRate: number;
  hourlyBillingRate: number;
  skillsRequired: string[];
  status: RequestStatus;
  urgency: 'Standard' | 'Urgent' | 'Emergency';
  location: string;
  specialInstructions?: string;
  createdAt: string;
}

export interface Assignment {
  id: string;
  assignmentCode: string;
  requestId: string;
  staffId: string;
  staffName: string;
  staffCategory: HealthcareRole;
  clientOrgName: string;
  startDate: string;
  endDate: string;
  shiftHours: string;
  status: AssignmentStatus;
  payRate: number;
  billingRate: number;
  supervisorName: string;
  checkInTime?: string;
  checkOutTime?: string;
}

export interface AttendanceRecord {
  id: string;
  assignmentId: string;
  staffName: string;
  staffCategory: HealthcareRole;
  clientOrgName: string;
  date: string;
  checkIn: string;
  checkOut: string;
  totalHours: number;
  overtimeHours: number;
  status: AttendanceStatus;
  locationVerified: boolean;
  notes?: string;
  approvedByClient: boolean;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  clientOrgName: string;
  billingPeriod: string;
  issueDate: string;
  dueDate: string;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  status: InvoiceStatus;
  itemCount: number;
}

export interface PaymentTransaction {
  id: string;
  transactionId: string;
  invoiceNumber: string;
  clientOrgName: string;
  paymentMethod: 'Bank Transfer' | 'Credit Card' | 'ACH' | 'Cheque';
  amount: number;
  paymentDate: string;
  status: PaymentStatus;
  notes?: string;
}

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  type: 'assignment' | 'approval' | 'attendance' | 'system' | 'payment';
  timestamp: string;
  read: boolean;
  link?: string;
}

export interface VerificationDocument {
  id: string;
  type: string;
  name: string;
  url: string;
  uploadedAt: string;
  status: 'Pending' | 'Verified' | 'Rejected';
  rejectionReason?: string;
}

export interface VerificationRequest {
  id: string;
  staffId: string;
  staffName: string;
  staffCategory: HealthcareRole;
  submittedAt: string;
  status: 'Pending' | 'Under Review' | 'Approved' | 'Rejected';
  documents: VerificationDocument[];
  adminNotes?: string;
  reviewedAt?: string;
  reviewedBy?: string;
}

export interface SystemMetric {
  title: string;
  value: string | number;
  changePercent?: number;
  trend?: 'up' | 'down' | 'neutral';
  description?: string;
}
