import apiClient from './apiClient';

export interface AttendanceResponse {
  id: number;
  assignmentId: number;
  staffName: string;
  staffEmail: string;
  designation: string;
  location: string;
  date: string;
  checkInTime: string;
  checkOutTime: string;
  workingHours: number;
  status: string;
  notes: string;
  createdAt: string;
}

export const attendanceService = {
  /** Request OTP for check-in — sends OTP to client email */
  requestCheckInOtp: async (assignmentId: number): Promise<string> => {
    const response = await apiClient.post<string>('/attendance/request-otp', null, {
      params: { assignmentId },
    });
    return response.data;
  },

  /** Staff check-in with OTP verification */
  checkIn: async (data: {
    assignmentId: number;
    otp: string;
    notes?: string;
  }): Promise<AttendanceResponse> => {
    const response = await apiClient.post('/attendance/checkin', data);
    return response.data;
  },

  /** Request checkout OTP — sends OTP to client email */
  requestCheckOutOtp: async (attendanceId: number): Promise<string> => {
    const response = await apiClient.post<string>('/attendance/request-checkout-otp', null, {
      params: { attendanceId },
    });
    return response.data;
  },

  /** Staff check-out with OTP verification */
  checkOut: async (
    attendanceId: number,
    data?: { notes?: string; otp?: string }
  ): Promise<AttendanceResponse> => {
    const response = await apiClient.post(`/attendance/checkout/${attendanceId}`, data || {});
    return response.data;
  },

  /** Get today's attendance */
  getToday: async (): Promise<AttendanceResponse> => {
    const response = await apiClient.get('/attendance/today');
    return response.data;
  },

  /** Get my attendance history */
  getMyAttendance: async (): Promise<AttendanceResponse[]> => {
    const response = await apiClient.get('/attendance/my');
    return response.data;
  },

  /** Get attendance by date range */
  getByDateRange: async (
    startDate: string,
    endDate: string
  ): Promise<AttendanceResponse[]> => {
    const response = await apiClient.get('/attendance/my/range', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  /** Get total working hours */
  getWorkingHours: async (
    startDate: string,
    endDate: string
  ): Promise<number> => {
    const response = await apiClient.get('/attendance/my/hours', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  /** Get attendance by assignment */
  getByAssignment: async (assignmentId: number): Promise<AttendanceResponse[]> => {
    const response = await apiClient.get(`/attendance/assignment/${assignmentId}`);
    return response.data;
  },
};
