package com.nurseadda.project.controller;

import com.nurseadda.project.common.exception.GlobalExceptionHandler;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.entity.Invoice;
import com.nurseadda.project.dto.response.InvoiceResponse;
import com.nurseadda.project.service.InvoicePdfService;
import com.nurseadda.project.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private InvoicePdfService invoicePdfService;

    @InjectMocks
    private InvoiceController invoiceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(invoiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =====================================================================
    //  GET /invoices/{id}/pdf — Download invoice PDF
    // =====================================================================

    @Test
    @DisplayName("GET /invoices/{id}/pdf: returns PDF file")
    void downloadInvoicePdf_valid_returns200() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setTotalAmount(new BigDecimal("4000.00"));
        invoice.setStatus("PENDING");

        when(invoiceService.getInvoiceEntityById(1L)).thenReturn(invoice);

        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        pdfStream.write("%PDF-1.4 fake content".getBytes());
        when(invoicePdfService.generateInvoicePdf(invoice)).thenReturn(pdfStream);

        mockMvc.perform(get("/api/invoices/1/pdf")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=invoice-000001.pdf"))
                .andExpect(content().contentType("application/pdf"));

        verify(invoiceService).getInvoiceEntityById(1L);
        verify(invoicePdfService).generateInvoicePdf(invoice);
    }

    @Test
    @DisplayName("GET /invoices/{id}/pdf: invoice not found returns 404")
    void downloadInvoicePdf_notFound_returns404() throws Exception {
        when(invoiceService.getInvoiceEntityById(999L))
                .thenThrow(new ResourceNotFoundException("Invoice not found"));

        mockMvc.perform(get("/api/invoices/999/pdf")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invoice not found"));
    }

    // =====================================================================
    //  GET /invoices — List invoices
    // =====================================================================

    @Test
    @DisplayName("GET /invoices: client sees their invoices")
    void getInvoices_client_returns200() throws Exception {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(1L);
        response.setTotalAmount(new BigDecimal("4000.00"));
        response.setStatus("PENDING");

        when(invoiceService.getClientInvoices("rahul@hospital.com"))
                .thenReturn(java.util.List.of(response));

        mockMvc.perform(get("/api/invoices")
                        .principal(new UsernamePasswordAuthenticationToken("rahul@hospital.com", null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(4000.00));

        verify(invoiceService).getClientInvoices("rahul@hospital.com");
    }

    // =====================================================================
    //  GET /billing/summary — Admin sees billing summary
    // =====================================================================

    @Test
    @DisplayName("GET /billing/summary: returns summary")
    void getBillingSummary_returns200() throws Exception {
        var summary = new com.nurseadda.project.dto.response.BillingSummaryResponse();
        summary.setTotalRevenue(new BigDecimal("50000.00"));
        summary.setPendingRevenue(new BigDecimal("10000.00"));
        summary.setTotalPaidToStaff(new BigDecimal("30000.00"));
        summary.setPendingStaffPayments(new BigDecimal("5000.00"));
        summary.setProfit(new BigDecimal("20000.00"));

        when(invoiceService.getBillingSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/billing/summary")
                        .principal(new UsernamePasswordAuthenticationToken("admin@nurse.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(50000.00))
                .andExpect(jsonPath("$.profit").value(20000.00));

        verify(invoiceService).getBillingSummary();
    }
}
