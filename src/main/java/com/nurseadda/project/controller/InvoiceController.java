package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.RateConfigRequest;
import com.nurseadda.project.dto.response.BillingSummaryResponse;
import com.nurseadda.project.dto.response.InvoiceResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.dto.response.RateConfigResponse;
import com.nurseadda.project.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/rates")
    public ResponseEntity<RateConfigResponse> createOrUpdateRate(@Valid @RequestBody RateConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createOrUpdateRateConfig(request));
    }

    @GetMapping("/rates")
    public ResponseEntity<List<RateConfigResponse>> getAllRates() {
        return ResponseEntity.ok(invoiceService.getAllRateConfigs());
    }

    @GetMapping("/rates/{shiftType}")
    public ResponseEntity<RateConfigResponse> getRateByShift(@PathVariable String shiftType) {
        return ResponseEntity.ok(invoiceService.getRateConfigByShift(shiftType));
    }

    @PostMapping("/invoices/generate/{attendanceId}")
    public ResponseEntity<InvoiceResponse> generateInvoice(@PathVariable Long attendanceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.generateInvoice(attendanceId));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("");
        return ResponseEntity.ok(role.equals("ROLE_USER") ? invoiceService.getClientInvoices(email) : invoiceService.getAllInvoices());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PatchMapping("/invoices/{id}/status")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, status));
    }

    @PostMapping("/payments/generate/{attendanceId}")
    public ResponseEntity<PaymentResponse> generatePayment(@PathVariable Long attendanceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.generatePayment(attendanceId));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getPayments(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("");
        return ResponseEntity.ok(role.equals("ROLE_STAFF") ? invoiceService.getStaffPayments(email) : invoiceService.getAllPayments());
    }

    @PatchMapping("/payments/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(invoiceService.updatePaymentStatus(id, status));
    }

    @GetMapping("/billing/summary")
    public ResponseEntity<BillingSummaryResponse> getBillingSummary() {
        return ResponseEntity.ok(invoiceService.getBillingSummary());
    }
}
