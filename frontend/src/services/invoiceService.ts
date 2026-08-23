import apiClient from './apiClient';

export interface InvoiceResponse {
  id: number;
  assignmentId: number;
  staffName: string;
  designation: string;
  location: string;
  baseHours: number;
  overtimeHours: number;
  clientHourlyRate: number;
  baseAmount: number;
  overtimeAmount: number;
  subtotal: number;
  gstRate: number;
  gstAmount: number;
  totalAmount: number;
  status: string;
  notes: string;
  createdAt: string;
}

export interface PaymentResponse {
  id: number;
  assignmentId: number;
  designation: string;
  location: string;
  baseHours: number;
  overtimeHours: number;
  staffHourlyRate: number;
  baseAmount: number;
  overtimeAmount: number;
  totalAmount: number;
  status: string;
  razorpayOrderId: string;
  razorpayPaymentId: string;
  notes: string;
  createdAt: string;
  refunded: boolean;
  refundAmount: number;
  refundPercentage: number;
  razorpayRefundId: string;
  refundedAt: string;
}

export interface RateConfigResponse {
  id: number;
  shiftType: string;
  clientHourlyRate: number;
  staffHourlyRate: number;
  overtimeMultiplier: number;
  createdAt: string;
  updatedAt: string;
}

export interface BillingSummaryResponse {
  totalBilled: number;
  totalPaid: number;
  totalPending: number;
  totalOverdue: number;
  invoiceCount: number;
  paymentCount: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ─── Invoices ───────────────────────────────────────────
export const invoiceService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<InvoiceResponse>> => {
    const response = await apiClient.get('/invoices', { params: { page, size } });
    return response.data;
  },

  getById: async (id: number): Promise<InvoiceResponse> => {
    const response = await apiClient.get(`/invoices/${id}`);
    return response.data;
  },

  generate: async (attendanceId: number): Promise<InvoiceResponse> => {
    const response = await apiClient.post(`/invoices/generate/${attendanceId}`);
    return response.data;
  },

  updateStatus: async (id: number, status: string): Promise<InvoiceResponse> => {
    const response = await apiClient.patch(`/invoices/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  downloadPdf: async (id: number): Promise<void> => {
    const response = await apiClient.get(`/invoices/${id}/pdf`, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `invoice-${String(id).padStart(6, '0')}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};

// ─── Payments ───────────────────────────────────────────
export const paymentService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<PaymentResponse>> => {
    const response = await apiClient.get('/payments', { params: { page, size } });
    return response.data;
  },

  generate: async (attendanceId: number): Promise<PaymentResponse> => {
    const response = await apiClient.post(`/payments/generate/${attendanceId}`);
    return response.data;
  },

  updateStatus: async (id: number, status: string): Promise<PaymentResponse> => {
    const response = await apiClient.patch(`/payments/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },
};

// ─── Rate Configs ───────────────────────────────────────
export const rateService = {
  getAll: async (): Promise<RateConfigResponse[]> => {
    const response = await apiClient.get('/rates');
    return response.data;
  },

  getByShift: async (shiftType: string): Promise<RateConfigResponse> => {
    const response = await apiClient.get(`/rates/${shiftType}`);
    return response.data;
  },

  createOrUpdate: async (data: {
    shiftType: string;
    clientHourlyRate: number;
    staffHourlyRate: number;
    overtimeMultiplier: number;
  }): Promise<RateConfigResponse> => {
    const response = await apiClient.post('/rates', data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/rates/${id}`);
  },
};

// ─── Billing ────────────────────────────────────────────
export const billingService = {
  getSummary: async (): Promise<BillingSummaryResponse> => {
    const response = await apiClient.get('/billing/summary');
    return response.data;
  },
};
