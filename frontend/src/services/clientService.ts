import apiClient from './apiClient';
import { UserResponse } from './authService';

export interface UserListResponse {
  content: UserResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const clientService = {
  /** Get all users (admin) */
  getAllUsers: async (page = 0, size = 20): Promise<UserListResponse> => {
    const response = await apiClient.get('/auth/admin/users', { params: { page, size } });
    return response.data;
  },

  /** Get own client profile */
  getMyProfile: async (): Promise<UserResponse> => {
    const response = await apiClient.get('/auth/client-profile');
    return response.data;
  },

  /** Update own client profile */
  updateProfile: async (data: {
    firstName: string;
    lastName: string;
    phone: string;
  }): Promise<UserResponse> => {
    const response = await apiClient.put('/auth/client-profile', data);
    return response.data;
  },

  /** Delete own client profile */
  deleteProfile: async (): Promise<string> => {
    const response = await apiClient.delete('/auth/client-profile');
    return response.data;
  },
};
