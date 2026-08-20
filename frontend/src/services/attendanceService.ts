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
  /** Staff check-in */
  checkIn: async (data: {
    assignmentId: number;
    location?: string;
    notes?: string;
  }): Promise<AttendanceResponse> => {
    const response = await apiClient.post('/attendance/checkin', data);
    return response.data;
  },

  /** Staff check-out */
  checkOut: async (
    attendanceId: number,
    data?: { notes?: string }
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
