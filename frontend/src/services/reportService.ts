import apiClient from './apiClient';

export interface AnalyticsResponse {
  totalStaff: number;
  verifiedStaff: number;
  totalClients: number;
  totalAssignments: number;
  activeAssignments: number;
  completedAssignments: number;
  totalRevenue: number;
  totalPaid: number;
  totalPending: number;
  totalInvoices: number;
  totalHoursWorked: number;
  staffByCategory: Record<string, number>;
  assignmentsByStatus: Record<string, number>;
}

export const reportService = {
  getAnalytics: async (): Promise<AnalyticsResponse> => {
    const response = await apiClient.get<AnalyticsResponse>('/analytics');
    return response.data;
  },
};
