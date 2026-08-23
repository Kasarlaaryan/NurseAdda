import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { UserRole } from '../types';
import { authService, UserResponse } from '../services/authService';
import { tokenStorage } from '../services/apiClient';

interface AuthState {
  user: UserResponse | null;
  isAuthenticated: boolean;
  activeRole: UserRole;
  isLoading: boolean;

  login: (email: string, password: string) => Promise<void>;
  registerClient: (data: {
    firstName: string;
    lastName: string;
    email: string;
    mobileNumber: string;
    password: string;
    confirmPassword: string;
  }) => Promise<string>;
  registerStaff: (data: {
    fullName: string;
    email: string;
    phone: string;
    staffCategory: string;
    password: string;
  }) => Promise<string>;
  verifyOtp: (email: string, otp: string) => Promise<string>;
  resendOtp: (email: string) => Promise<string>;
  logout: () => Promise<void>;
  fetchCurrentUser: () => Promise<void>;
  setActiveRole: (role: UserRole) => void;
  completeProfile: () => void;
  setUser: (user: UserResponse) => void;
}

/** Map backend role enum to frontend UserRole */
function mapRole(backendRole: string): UserRole {
  const roleMap: Record<string, UserRole> = {
    ROLE_SUPER_ADMIN: 'ROLE_SUPER_ADMIN',
    ROLE_ADMIN: 'ROLE_ADMIN',
    ROLE_STAFF: 'ROLE_STAFF',
    ROLE_USER: 'ROLE_USER',
  };
  return roleMap[backendRole] || 'ROLE_USER';
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      activeRole: 'ROLE_USER' as UserRole,
      isLoading: false,

      login: async (email: string, password: string) => {
        set({ isLoading: true });
        try {
          const response = await authService.login(email, password);
          tokenStorage.setTokens(response.accessToken, response.refreshToken);
          const user = {
            ...response.user,
            name: `${response.user.firstName} ${response.user.lastName}`,
          };
          set({
            user,
            isAuthenticated: true,
            activeRole: mapRole(response.user.role),
            isLoading: false,
          });
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },

      registerClient: async (data) => {
        set({ isLoading: true });
        try {
          const message = await authService.registerClient(data);
          set({ isLoading: false });
          return message;
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },

      registerStaff: async (data) => {
        set({ isLoading: true });
        try {
          const message = await authService.registerStaff(data);
          set({ isLoading: false });
          return message;
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },

      verifyOtp: async (email, otp) => {
        const message = await authService.verifyOtp(email, otp);
        return message;
      },

      resendOtp: async (email) => {
        const message = await authService.resendOtp(email);
        return message;
      },

      logout: async () => {
        try {
          await authService.logout();
        } catch {
          // Logout even if API call fails
        }
        tokenStorage.clearTokens();
        set({
          user: null,
          isAuthenticated: false,
          activeRole: 'ROLE_USER',
        });
      },

      fetchCurrentUser: async () => {
        try {
          const userData = await authService.getMe();
          const user = {
            ...userData,
            name: `${userData.firstName} ${userData.lastName}`,
          };
          set({
            user,
            isAuthenticated: true,
            activeRole: mapRole(user.role),
          });
        } catch {
          tokenStorage.clearTokens();
          set({
            user: null,
            isAuthenticated: false,
          });
        }
      },

      setActiveRole: (role) => set({ activeRole: role }),

      completeProfile: () => {
        set((state) => ({
          user: state.user ? { ...state.user, isProfileComplete: true } : null,
        }));
      },

      setUser: (user) =>
        set({
          user,
          activeRole: mapRole(user.role),
        }),
    }),
    {
      name: 'nurseadda-auth-storage',
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
        activeRole: state.activeRole,
      }),
    }
  )
);
