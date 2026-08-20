import apiClient from './apiClient';

// ─── Types matching backend DTOs ────────────────────────
export interface AssignmentResponse {
  id: number;
  staffProfileId: number;
  staffName: string;
  staffEmail: string;
  staffCategory: string;
  staffingRequestId: number;
  designation: string;
  location: string;
  shift: string;
  assignedByName: string;
  status: string;
  notes: string;
  sentToClient: boolean;
  sentToClientAt: string;
  acceptedAt: string;
  completedAt: string;
  createdAt: string;
}

export interface StaffingRequestResponse {
  id: number;
  clientName: string;
  designation: string;
  location: string;
  requestType: string;
  shift: string;
  startDate: string;
  endDate: string;
  numberOfStaff: number;
  requiredSkills: string;
  status: string;
  deadline: string;
  overdue: boolean;
  hoursRemaining: number;
  minutesRemaining: number;
  estimatedTotal: number;
  advanceAmount: number;
  advancePaid: boolean;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ─── Staffing Requests ──────────────────────────────────
export const staffingRequestService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<StaffingRequestResponse>> => {
    const response = await apiClient.get('/staffing-requests', { params: { page, size } });
    return response.data;
  },

  getById: async (id: number): Promise<StaffingRequestResponse> => {
    const response = await apiClient.get(`/staffing-requests/${id}`);
    return response.data;
  },

  create: async (data: {
    designation: string;
    location: string;
    requestType: 'ON_CALL' | 'MONTHLY';
    shift: string;
    startDate: string;
    endDate: string;
    numberOfStaff: number;
    requiredSkills: string;
  }): Promise<StaffingRequestResponse> => {
    const response = await apiClient.post('/staffing-requests', data);
    return response.data;
  },

  updateStatus: async (
    id: number,
    status: string
  ): Promise<StaffingRequestResponse> => {
    const response = await apiClient.patch(`/staffing-requests/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  getPendingForStaff: async (): Promise<PageResponse<StaffingRequestResponse>> => {
    const response = await apiClient.get('/staffing-requests/pending');
    return response.data;
  },

  accept: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.post(`/staffing-requests/${id}/accept`);
    return response.data;
  },

  getByStatus: async (
    status: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<StaffingRequestResponse>> => {
    const response = await apiClient.get(`/staffing-requests/status/${status}`, {
      params: { page, size },
    });
    return response.data;
  },
};

// ─── Assignments ────────────────────────────────────────
export const assignmentService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<AssignmentResponse>> => {
    const response = await apiClient.get('/assignments', { params: { page, size } });
    return response.data;
  },

  getById: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.get(`/assignments/${id}`);
    return response.data;
  },

  create: async (data: {
    staffingRequestId: number;
    staffProfileId: number;
    notes?: string;
  }): Promise<AssignmentResponse> => {
    const response = await apiClient.post('/assignments', data);
    return response.data;
  },

  updateStatus: async (
    id: number,
    status: string,
    notes?: string
  ): Promise<AssignmentResponse> => {
    const response = await apiClient.put(`/assignments/${id}/status`, { status, notes });
    return response.data;
  },

  getByStatus: async (status: string): Promise<AssignmentResponse[]> => {
    const response = await apiClient.get(`/assignments/status/${status}`);
    return response.data;
  },

  approveToClient: async (id: number): Promise<AssignmentResponse> => {
    const response = await apiClient.patch(`/assignments/${id}/approve-to-client`);
    return response.data;
  },

  getMyClientAssignments: async (
    page = 0,
    size = 20
  ): Promise<PageResponse<AssignmentResponse>> => {
    const response = await apiClient.get('/assignments/my-clients', {
      params: { page, size },
    });
    return response.data;
  },

  getRequestAssignments: async (
    requestId: number
  ): Promise<AssignmentResponse[]> => {
    const response = await apiClient.get(`/staffing-requests/${requestId}/assignments`);
    return response.data;
  },
};
