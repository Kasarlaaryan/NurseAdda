import apiClient from './apiClient';

// ─── Types matching backend DTOs ────────────────────────
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
}

export interface UserResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: string;
  /** Computed full name */
  name?: string;
  /** Computed display properties for UI compatibility */
  avatarUrl?: string;
  organizationName?: string;
  staffCategory?: string;
  department?: string;
  isProfileComplete?: boolean;
}

export interface StaffProfileResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  staffCategory: string;
  aadharCardNumber: string;
  location?: string;
  licenseValidityDate: string;
  licenseRenewalDate: string;
  verified: boolean;
  /** EXPIRED, EXPIRING_SOON, VALID, or UNKNOWN */
  licenseStatus?: string;
  /** Reason provided by admin when verification was removed */
  rejectionReason?: string;
  stateBoardCertificatePath: string;
  educationalDocumentPaths: string[];
  photoPaths: string[];
}

// ─── Auth API calls ─────────────────────────────────────
export const authService = {
  /** Register a new client */
  registerClient: async (data: {
    firstName: string;
    lastName: string;
    email: string;
    mobileNumber: string;
    password: string;
    confirmPassword: string;
  }): Promise<string> => {
    const response = await apiClient.post<string>('/auth/register-client', data);
    return response.data;
  },

  /** Register a new staff member */
  registerStaff: async (data: {
    fullName: string;
    email: string;
    phone: string;
    staffCategory: string;
    password: string;
  }): Promise<string> => {
    const response = await apiClient.post<string>('/auth/register-staff', data);
    return response.data;
  },

  /** Send OTP to email */
  sendOtp: async (email: string): Promise<string> => {
    const response = await apiClient.post<string>('/auth/send-otp', { email });
    return response.data;
  },

  /** Verify OTP */
  verifyOtp: async (email: string, otp: string): Promise<string> => {
    const response = await apiClient.post<string>('/auth/verify-otp', { email, otp });
    return response.data;
  },

  /** Resend OTP */
  resendOtp: async (email: string): Promise<string> => {
    const response = await apiClient.post<string>('/auth/resend-otp', { email });
    return response.data;
  },

  /** Login */
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', {
      email,
      password,
    });
    return response.data;
  },

  /** Refresh access token */
  refresh: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/refresh', {
      refreshToken,
    });
    return response.data;
  },

  /** Get current user profile */
  getMe: async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>('/auth/me');
    return response.data;
  },

  /** Logout */
  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },

  /** Forgot password */
  forgotPassword: async (email: string): Promise<string> => {
    const response = await apiClient.post<string>('/auth/forgot-password', { email });
    return response.data;
  },

  /** Reset password */
  resetPassword: async (data: {
    email: string;
    otp: string;
    newPassword: string;
    confirmPassword: string;
  }): Promise<string> => {
    const response = await apiClient.post<string>('/auth/reset-password', data);
    return response.data;
  },

  /** Change password (authenticated) */
  changePassword: async (data: {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
  }): Promise<string> => {
    const response = await apiClient.post<string>('/auth/change-password', data);
    return response.data;
  },

  /** Get staff profile */
  getStaffProfile: async (): Promise<StaffProfileResponse> => {
    const response = await apiClient.get<StaffProfileResponse>('/auth/staff-profile');
    return response.data;
  },

  /** Get client profile */
  getClientProfile: async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>('/auth/client-profile');
    return response.data;
  },

  /** Update client profile */
  updateClientProfile: async (data: {
    firstName: string;
    lastName: string;
    phone: string;
  }): Promise<UserResponse> => {
    const response = await apiClient.put<UserResponse>('/auth/client-profile', data);
    return response.data;
  },

  /** Get all staff profiles (admin) */
  getAllStaffProfiles: async (
    page = 0,
    size = 20
  ): Promise<{ content: StaffProfileResponse[]; totalElements: number; totalPages: number }> => {
    const response = await apiClient.get('/auth/staff', {
      params: { page, size },
    });
    return response.data;
  },

  /** Get all users (admin) */
  getAllUsers: async (
    page = 0,
    size = 20
  ): Promise<{ content: UserResponse[]; totalElements: number; totalPages: number }> => {
    const response = await apiClient.get('/auth/admin/users', {
      params: { page, size },
    });
    return response.data;
  },

  /** Register a new admin (super admin only) */
  registerAdmin: async (data: {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    password: string;
    confirmPassword: string;
    adminType: 'ADMIN' | 'SUPER_ADMIN';
  }): Promise<string> => {
    const response = await apiClient.post<string>('/auth/register-admin', data);
    return response.data;
  },

  /** Update staff profile with document uploads */
  updateStaffProfile: async (data: {
    aadharCardNumber?: string;
    location?: string;
    licenseValidityDate?: string;
    licenseRenewalDate?: string;
    stateBoardCertificate?: File;
    educationalDocuments?: File[];
    photos?: File[];
  }): Promise<StaffProfileResponse> => {
    const formData = new FormData();

    const profilePayload: Record<string, string> = {};
    if (data.aadharCardNumber) profilePayload.aadharCardNumber = data.aadharCardNumber;
    if (data.location) profilePayload.location = data.location;
    if (data.licenseValidityDate) profilePayload.licenseValidityDate = data.licenseValidityDate;
    if (data.licenseRenewalDate) profilePayload.licenseRenewalDate = data.licenseRenewalDate;

    formData.append('profile', new Blob([JSON.stringify(profilePayload)], { type: 'application/json' }));

    if (data.stateBoardCertificate) {
      formData.append('stateBoardCertificate', data.stateBoardCertificate);
    }
    if (data.educationalDocuments) {
      data.educationalDocuments.forEach((file) => {
        formData.append('educationalDocuments', file);
      });
    }
    if (data.photos) {
      data.photos.forEach((file) => {
        formData.append('photos', file);
      });
    }

    const response = await apiClient.put<StaffProfileResponse>('/auth/staff-profile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};
