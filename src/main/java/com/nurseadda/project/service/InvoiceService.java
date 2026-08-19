package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.RateConfigRequest;
import com.nurseadda.project.dto.response.BillingSummaryResponse;
import com.nurseadda.project.dto.response.InvoiceResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.dto.response.RateConfigResponse;

import java.util.List;

public interface InvoiceService {
    RateConfigResponse createOrUpdateRateConfig(RateConfigRequest request);
    List<RateConfigResponse> getAllRateConfigs();
    RateConfigResponse getRateConfigByShift(String shiftType);
    InvoiceResponse generateInvoice(Long attendanceId);
    List<InvoiceResponse> getClientInvoices(String clientEmail);
    List<InvoiceResponse> getAllInvoices();
    InvoiceResponse updateInvoiceStatus(Long invoiceId, String status);
    InvoiceResponse getInvoiceById(Long invoiceId);
    PaymentResponse generatePayment(Long attendanceId);
    List<PaymentResponse> getStaffPayments(String staffEmail);
    List<PaymentResponse> getAllPayments();
    PaymentResponse updatePaymentStatus(Long paymentId, String status);
    BillingSummaryResponse getBillingSummary();
}
