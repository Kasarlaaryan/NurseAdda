package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.RateConfigRequest;
import com.nurseadda.project.dto.response.BillingSummaryResponse;
import com.nurseadda.project.dto.response.InvoiceResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.dto.response.RateConfigResponse;
import com.nurseadda.project.entity.Invoice;

import com.nurseadda.project.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceService {
    RateConfigResponse createOrUpdateRateConfig(RateConfigRequest request, String adminEmail);
    void deleteRateConfig(Long rateConfigId, String adminEmail);
    List<RateConfigResponse> getAllRateConfigs();
    RateConfigResponse getRateConfigByShift(String shiftType);
    InvoiceResponse generateInvoice(Long attendanceId);
    List<InvoiceResponse> getClientInvoices(String clientEmail);
    PageResponse<InvoiceResponse> getClientInvoices(String clientEmail, Pageable pageable);
    List<InvoiceResponse> getAllInvoices();
    PageResponse<InvoiceResponse> getAllInvoices(Pageable pageable);
    InvoiceResponse updateInvoiceStatus(Long invoiceId, String status);
    InvoiceResponse getInvoiceById(Long invoiceId);
    Invoice getInvoiceEntityById(Long invoiceId);
    PaymentResponse generatePayment(Long attendanceId);
    List<PaymentResponse> getStaffPayments(String staffEmail);
    PageResponse<PaymentResponse> getStaffPayments(String staffEmail, Pageable pageable);
    List<PaymentResponse> getAllPayments();
    PageResponse<PaymentResponse> getAllPayments(Pageable pageable);
    PaymentResponse updatePaymentStatus(Long paymentId, String status);
    BillingSummaryResponse getBillingSummary();
}
