import { HealthcareStaff, ClientOrganization, StaffingRequest, Assignment, AttendanceRecord, Invoice, PaymentTransaction, NotificationItem, VerificationRequest, User } from '../types';

export const MOCK_STAFF_LIST: HealthcareStaff[] = [
  {
    id: 'stf-101',
    fullName: 'Priya Sharma, RN',
    email: 'priya.sharma@nurseadda.com',
    phone: '+1 (555) 345-6789',
    category: 'Registered Nurse (RN)',
    status: 'On Assignment',
    skills: ['ICU Care', 'Ventilator Management', 'Cardiothoracic Support', 'IV Infusion'],
    experienceYears: 7,
    rating: 4.9,
    completedShifts: 142,
    hourlyRate: 58,
    location: 'Metro Central - East Wing',
    verificationStatus: 'Verified',
    avatarUrl: 'https://images.unsplash.com/photo-1594824813566-7885a3964582?w=150&auto=format&fit=crop&q=80',
    certifications: [
      { id: 'c1', name: 'Registered Nurse (RN) License', expiryDate: '2027-12-31', isVerified: true },
      { id: 'c2', name: 'Advanced Cardiac Life Support (ACLS)', expiryDate: '2026-09-15', isVerified: true },
      { id: 'c3', name: 'Pediatric Advanced Life Support (PALS)', expiryDate: '2025-11-20', isVerified: true }
    ],
    education: 'B.Sc. Nursing - St. Marys College of Nursing',
    availability: 'Immediate',
    currentAssignment: 'City General Hospital - ICU Dept',
    documentsCount: 6,
    joinedDate: '2023-02-14'
  },
  {
    id: 'stf-102',
    fullName: 'Marcus Vance, PT',
    email: 'marcus.vance@nurseadda.com',
    phone: '+1 (555) 456-7890',
    category: 'Physiotherapist',
    status: 'Active',
    skills: ['Post-Surgical Rehab', 'Geriatric Orthopedics', 'Neurological Therapy'],
    experienceYears: 5,
    rating: 4.8,
    completedShifts: 98,
    hourlyRate: 52,
    location: 'Westside Rehab Clinic',
    verificationStatus: 'Verified',
    avatarUrl: 'https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=150&auto=format&fit=crop&q=80',
    certifications: [
      { id: 'c4', name: 'Licensed Physical Therapist', expiryDate: '2026-10-30', isVerified: true },
      { id: 'c5', name: 'Dry Needling Certification', expiryDate: '2027-05-10', isVerified: true }
    ],
    education: 'Doctor of Physical Therapy - State Health University',
    availability: 'Weekdays',
    documentsCount: 4,
    joinedDate: '2023-06-01'
  },
  {
    id: 'stf-103',
    fullName: 'Anita Desai',
    email: 'anita.desai@nurseadda.com',
    phone: '+1 (555) 567-8901',
    category: 'Auxiliary Nurse Midwifery (ANM)',
    status: 'Active',
    skills: ['Elderly Care', 'Dementia Care', 'Medication Management', 'Mobility Assistance'],
    experienceYears: 4,
    rating: 4.7,
    completedShifts: 180,
    hourlyRate: 34,
    location: 'North Suburban District',
    verificationStatus: 'Verified',
    avatarUrl: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80',
    certifications: [
      { id: 'c6', name: 'Certified Nursing Assistant (CNA)', expiryDate: '2026-08-01', isVerified: true },
      { id: 'c7', name: 'BLS Certification', expiryDate: '2026-01-15', isVerified: true }
    ],
    education: 'Certified Caregiver Diploma - Healthcare Vocational Institute',
    availability: 'Night Shifts',
    documentsCount: 5,
    joinedDate: '2022-11-10'
  },
  {
    id: 'stf-104',
    fullName: 'David Miller',
    email: 'david.miller@nurseadda.com',
    phone: '+1 (555) 678-9012',
    category: 'Medical Lab Technician',
    status: 'Pending Verification',
    skills: ['Hematology Analysis', 'PCR Diagnostics', 'Blood Bank Management'],
    experienceYears: 3,
    rating: 4.6,
    completedShifts: 42,
    hourlyRate: 40,
    location: 'Central Diagnostic Lab',
    verificationStatus: 'Pending',
    avatarUrl: 'https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=150&auto=format&fit=crop&q=80',
    certifications: [
      { id: 'c8', name: 'Medical Laboratory Technologist', expiryDate: '2025-09-01', isVerified: false }
    ],
    education: 'B.Sc. Medical Lab Technology',
    availability: 'Weekends',
    documentsCount: 3,
    joinedDate: '2024-01-15'
  },
  {
    id: 'stf-105',
    fullName: 'Elena Rostova, BS',
    email: 'elena.r@nurseadda.com',
    phone: '+1 (555) 789-0123',
    category: 'Medical Lab Technician',
    status: 'On Assignment',
    skills: ['Dialysis Operation', 'ECHO Scan Tech', 'Sterilization Safety'],
    experienceYears: 6,
    rating: 4.9,
    completedShifts: 115,
    hourlyRate: 46,
    location: 'Apex Dialysis Center',
    verificationStatus: 'Verified',
    avatarUrl: 'https://images.unsplash.com/photo-1582750433449-648ed127bb54?w=150&auto=format&fit=crop&q=80',
    certifications: [
      { id: 'c9', name: 'Certified Hemodialysis Technologist', expiryDate: '2027-01-20', isVerified: true }
    ],
    education: 'Diploma in Dialysis Technology',
    availability: 'Immediate',
    currentAssignment: 'Apex Healthcare - Renal Unit',
    documentsCount: 5,
    joinedDate: '2023-04-12'
  }
];

export const MOCK_CLIENT_ORGS: ClientOrganization[] = [
  {
    id: 'org-201',
    name: 'City General Hospital',
    type: 'Hospital',
    email: 'staffing@citygeneralhospital.org',
    phone: '+1 (555) 901-2345',
    address: '450 Healthcare Blvd, Suite 100',
    city: 'Metropolis',
    branchesCount: 4,
    activeRequestsCount: 5,
    activeStaffCount: 22,
    primaryContactName: 'Robert Sterling',
    primaryContactRole: 'Director of Nursing Services',
    contractStatus: 'Active',
    logoUrl: 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=150&auto=format&fit=crop&q=80',
    totalBilled: 142500,
    outstandingBalance: 18400
  },
  {
    id: 'org-202',
    name: 'Apex Care Clinic Group',
    type: 'Clinic',
    email: 'ops@apexclinic.com',
    phone: '+1 (555) 890-1234',
    address: '120 Wellness Way',
    city: 'Eastville',
    branchesCount: 2,
    activeRequestsCount: 2,
    activeStaffCount: 8,
    primaryContactName: 'Dr. Evelyn Reed',
    primaryContactRole: 'Medical Administrator',
    contractStatus: 'Active',
    logoUrl: 'https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=150&auto=format&fit=crop&q=80',
    totalBilled: 68400,
    outstandingBalance: 0
  },
  {
    id: 'org-203',
    name: 'BioHealth Corporate Solutions',
    type: 'Corporate Healthcare Organization',
    email: 'wellness@biohealth.corp',
    phone: '+1 (555) 765-4321',
    address: '88 Tech Park Drive',
    city: 'Innovation Hub',
    branchesCount: 1,
    activeRequestsCount: 1,
    activeStaffCount: 4,
    primaryContactName: 'Karen Miller',
    primaryContactRole: 'VP Employee Health',
    contractStatus: 'Active',
    logoUrl: 'https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=150&auto=format&fit=crop&q=80',
    totalBilled: 39100,
    outstandingBalance: 4200
  }
];

export const MOCK_STAFFING_REQUESTS: StaffingRequest[] = [
  {
    id: 'req-301',
    requestNumber: 'REQ-2026-089',
    clientOrgId: 'org-201',
    clientOrgName: 'City General Hospital',
    requiredRole: 'Registered Nurse (RN)',
    quantityNeeded: 5,
    fulfilledQuantity: 3,
    shiftType: 'Night Shift',
    startDate: '2026-08-01',
    endDate: '2026-08-15',
    hourlyPayRate: 58,
    hourlyBillingRate: 82,
    skillsRequired: ['ICU Care', 'Ventilator Management', 'ACLS'],
    status: 'Partially Filled',
    urgency: 'Urgent',
    location: 'Metropolis Main Campus - ICU 3rd Floor',
    specialInstructions: 'Must possess active ACLS license and minimum 3 years ICU ICU experience.',
    createdAt: '2026-07-28'
  },
  {
    id: 'req-302',
    requestNumber: 'REQ-2026-090',
    clientOrgId: 'org-202',
    clientOrgName: 'Apex Care Clinic Group',
    requiredRole: 'Physiotherapist',
    quantityNeeded: 2,
    fulfilledQuantity: 2,
    shiftType: 'Day Shift',
    startDate: '2026-08-02',
    endDate: '2026-08-30',
    hourlyPayRate: 52,
    hourlyBillingRate: 75,
    skillsRequired: ['Post-Surgical Rehab', 'Orthopedics'],
    status: 'Fulfilled',
    urgency: 'Standard',
    location: 'Eastville Clinic Branch B',
    createdAt: '2026-07-25'
  },
  {
    id: 'req-303',
    requestNumber: 'REQ-2026-091',
    clientOrgId: 'org-201',
    clientOrgName: 'City General Hospital',
    requiredRole: 'Auxiliary Nurse Midwifery (ANM)',
    quantityNeeded: 4,
    fulfilledQuantity: 0,
    shiftType: 'Rotational',
    startDate: '2026-08-05',
    endDate: '2026-08-25',
    hourlyPayRate: 34,
    hourlyBillingRate: 50,
    skillsRequired: ['Elderly Care', 'Mobility Assistance'],
    status: 'Open',
    urgency: 'Emergency',
    location: 'Geriatric Ward - Building C',
    createdAt: '2026-07-29'
  }
];

export const MOCK_ASSIGNMENTS: Assignment[] = [
  {
    id: 'asg-401',
    assignmentCode: 'ASG-9921',
    requestId: 'req-301',
    staffId: 'stf-101',
    staffName: 'Priya Sharma, RN',
    staffCategory: 'Registered Nurse (RN)',
    clientOrgName: 'City General Hospital',
    startDate: '2026-07-31',
    endDate: '2026-08-15',
    shiftHours: '19:00 - 07:00 (12h Night)',
    status: 'Accepted',
    payRate: 58,
    billingRate: 82,
    supervisorName: 'Head Nurse Brenda Vance'
  },
  {
    id: 'asg-402',
    assignmentCode: 'ASG-9922',
    requestId: 'req-302',
    staffId: 'stf-102',
    staffName: 'Marcus Vance, PT',
    staffCategory: 'Physiotherapist',
    clientOrgName: 'Apex Care Clinic Group',
    startDate: '2026-07-30',
    endDate: '2026-08-30',
    shiftHours: '08:00 - 16:30 (8.5h Day)',
    status: 'In Progress',
    payRate: 52,
    billingRate: 75,
    supervisorName: 'Dr. Evelyn Reed'
  },
  {
    id: 'asg-403',
    assignmentCode: 'ASG-9923',
    requestId: 'req-303',
    staffId: 'stf-103',
    staffName: 'Ankit Verma',
    staffCategory: 'Medical Lab Technician',
    clientOrgName: 'Pacific Wellness Center',
    startDate: '2026-07-29',
    endDate: '2026-07-29',
    shiftHours: '09:00 - 17:00 (8h Day)',
    status: 'Completed',
    payRate: 40,
    billingRate: 60,
    supervisorName: 'Lab Mgr. Steven Strange'
  },
  {
    id: 'asg-404',
    assignmentCode: 'ASG-9924',
    requestId: 'req-304',
    staffId: 'stf-101',
    staffName: 'Priya Sharma, RN',
    staffCategory: 'Registered Nurse (RN)',
    clientOrgName: 'City General Hospital',
    startDate: '2026-07-30',
    endDate: '2026-07-30',
    shiftHours: '07:00 - 19:00 (12h Day)',
    status: 'In Progress',
    payRate: 60,
    billingRate: 85,
    supervisorName: 'Nurse Mgr. Robert Brown'
  }
];

export const MOCK_VERIFICATION_REQUESTS: VerificationRequest[] = [
  {
    id: 'ver-001',
    staffId: 'stf-104',
    staffName: 'Karan Mehra',
    staffCategory: 'Registered Nurse (RN)',
    submittedAt: '2026-07-30T10:30:00Z',
    status: 'Pending',
    documents: [
      {
        id: 'doc-1',
        type: 'Professional License',
        name: 'nursing_license_karan.pdf',
        url: '#',
        uploadedAt: '2026-07-30T10:25:00Z',
        status: 'Pending'
      },
      {
        id: 'doc-2',
        type: 'Identity Proof',
        name: 'passport_copy_karan.jpg',
        url: '#',
        uploadedAt: '2026-07-30T10:28:00Z',
        status: 'Pending'
      }
    ]
  },
  {
    id: 'ver-002',
    staffId: 'stf-105',
    staffName: 'Sonal Gupta',
    staffCategory: 'Medical Lab Technician',
    submittedAt: '2026-07-29T15:45:00Z',
    status: 'Under Review',
    documents: [
      {
        id: 'doc-3',
        type: 'Professional License',
        name: 'tech_cert_sonal.pdf',
        url: '#',
        uploadedAt: '2026-07-29T15:40:00Z',
        status: 'Verified'
      },
      {
        id: 'doc-4',
        type: 'Identity Proof',
        name: 'aadhar_sonal.png',
        url: '#',
        uploadedAt: '2026-07-29T15:42:00Z',
        status: 'Pending'
      }
    ],
    adminNotes: 'Checking ID document clarity.'
  }
];

export const MOCK_ATTENDANCE: AttendanceRecord[] = [
  {
    id: 'att-501',
    assignmentId: 'asg-401',
    staffName: 'Priya Sharma, RN',
    staffCategory: 'Registered Nurse (RN)',
    clientOrgName: 'City General Hospital',
    date: '2026-07-29',
    checkIn: '18:52',
    checkOut: '07:08',
    totalHours: 12.2,
    overtimeHours: 0.2,
    status: 'Checked Out',
    locationVerified: true,
    notes: 'Shift handed over to morning charge nurse.',
    approvedByClient: true
  },
  {
    id: 'att-502',
    assignmentId: 'asg-402',
    staffName: 'Marcus Vance, PT',
    staffCategory: 'Physiotherapist',
    clientOrgName: 'Apex Care Clinic Group',
    date: '2026-07-30',
    checkIn: '07:58',
    checkOut: 'Pending',
    totalHours: 5.5,
    overtimeHours: 0,
    status: 'Checked In',
    locationVerified: true,
    approvedByClient: false
  }
];

export const MOCK_INVOICES: Invoice[] = [
  {
    id: 'inv-601',
    invoiceNumber: 'INV-2026-0041',
    clientOrgName: 'City General Hospital',
    billingPeriod: 'July 01 - July 15, 2026',
    issueDate: '2026-07-16',
    dueDate: '2026-08-15',
    subtotal: 16800,
    taxAmount: 1600,
    totalAmount: 18400,
    paidAmount: 0,
    status: 'Sent',
    itemCount: 14
  },
  {
    id: 'inv-602',
    invoiceNumber: 'INV-2026-0038',
    clientOrgName: 'Apex Care Clinic Group',
    billingPeriod: 'July 01 - July 15, 2026',
    issueDate: '2026-07-16',
    dueDate: '2026-08-01',
    subtotal: 12000,
    taxAmount: 1200,
    totalAmount: 13200,
    paidAmount: 13200,
    status: 'Paid',
    itemCount: 8
  }
];

export const MOCK_PAYMENTS: PaymentTransaction[] = [
  {
    id: 'pmt-701',
    transactionId: 'TXN-882910',
    invoiceNumber: 'INV-2026-0038',
    clientOrgName: 'Apex Care Clinic Group',
    paymentMethod: 'Bank Transfer',
    amount: 13200,
    paymentDate: '2026-07-22',
    status: 'Completed',
    notes: 'ACH Direct Deposit Ref #99281'
  },
  {
    id: 'pmt-702',
    transactionId: 'TXN-882911',
    invoiceNumber: 'INV-2026-0041',
    clientOrgName: 'City General Hospital',
    paymentMethod: 'ACH',
    amount: 5000,
    paymentDate: '2026-07-28',
    status: 'Processing',
    notes: 'Partial invoice payment pending clearance'
  }
];

export const MOCK_NOTIFICATIONS: NotificationItem[] = [
  {
    id: 'notif-1',
    title: 'New Emergency Staffing Request',
    message: 'City General Hospital submitted an urgent request for 4 Caregivers.',
    type: 'assignment',
    timestamp: '10 minutes ago',
    read: false,
    link: '/requests'
  },
  {
    id: 'notif-2',
    title: 'Credential Verification Required',
    message: 'Lab Technician David Miller uploaded a new laboratory license.',
    type: 'approval',
    timestamp: '1 hour ago',
    read: false,
    link: '/staff'
  },
  {
    id: 'notif-3',
    title: 'Shift Overtime Alert',
    message: 'Nurse Priya Sharma logged 1.5h overtime at ICU Unit 3.',
    type: 'attendance',
    timestamp: '3 hours ago',
    read: true,
    link: '/attendance'
  },
  {
    id: 'notif-4',
    title: 'Invoice Payment Received',
    message: 'Apex Care Clinic cleared Invoice #INV-2026-0038 ($13,200).',
    type: 'payment',
    timestamp: 'Yesterday',
    read: true,
    link: '/payments'
  }
];

export const MOCK_USERS: User[] = [
  {
    id: 'usr-admin-01',
    name: 'Dr. Sarah Jenkins',
    email: 'sarah.jenkins@nurseadda.com',
    role: 'ROLE_SUPER_ADMIN',
    department: 'Executive Operations',
    phone: '+1 (555) 234-5678',
    isProfileComplete: true,
    avatarUrl: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=150&auto=format&fit=crop&q=80'
  },
  {
    id: 'usr-ops-02',
    name: 'Michael Vance',
    email: 'm.vance@nurseadda.com',
    role: 'ROLE_ADMIN',
    department: 'Staff Allocation Unit',
    phone: '+1 (555) 876-5432',
    isProfileComplete: true,
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80'
  },
  {
    id: 'usr-staff-03',
    name: 'Nurse Priya Sharma, RN',
    email: 'priya.sharma@nurseadda.com',
    role: 'ROLE_STAFF',
    department: 'ICU & Critical Care',
    phone: '+1 (555) 345-6789',
    isProfileComplete: true,
    avatarUrl: 'https://images.unsplash.com/photo-1594824813566-7885a3964582?w=150&auto=format&fit=crop&q=80'
  },
  {
    id: 'usr-client-04',
    name: 'Robert Sterling',
    email: 'r.sterling@citygeneralhospital.org',
    role: 'ROLE_USER',
    organizationName: 'City General Hospital',
    department: 'Human Resources & Nursing Admin',
    phone: '+1 (555) 901-2345',
    isProfileComplete: true,
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80'
  }
];
