import apiClient from './apiClient';
import { StaffProfileResponse } from './authService';

export interface StaffListResponse {
  content: StaffProfileResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const staffService = {
  /** Get all staff profiles (admin) */
  getAllStaff: async (page = 0, size = 20): Promise<StaffListResponse> => {
    const response = await apiClient.get('/auth/staff', { params: { page, size } });
    return response.data;
  },

  /** Get staff profile by ID (admin) */
  getStaffById: async (id: number): Promise<StaffProfileResponse> => {
    const response = await apiClient.get(`/auth/staff/${id}`);
    return response.data;
  },

  /** Get own staff profile (staff user) */
  getMyProfile: async (): Promise<StaffProfileResponse> => {
    const response = await apiClient.get('/auth/staff-profile');
    return response.data;
  },

  /** Verify / unverify a staff member (admin) */
  verifyStaff: async (
    userId: number,
    verified: boolean
  ): Promise<StaffProfileResponse> => {
    const response = await apiClient.patch(`/auth/staff/${userId}/verification`, {
      verified,
    });
    return response.data;
  },

  /** Unlock a locked account (admin) */
  unlockAccount: async (userId: number): Promise<string> => {
    const response = await apiClient.patch(`/auth/users/${userId}/unlock`);
    return response.data;
  },

  /** Delete a user (admin) */
  deleteUser: async (userId: number): Promise<string> => {
    const response = await apiClient.delete(`/auth/users/${userId}`);
    return response.data;
  },
};
