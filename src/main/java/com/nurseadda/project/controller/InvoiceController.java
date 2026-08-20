package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.RateConfigRequest;
import com.nurseadda.project.dto.response.BillingSummaryResponse;
import com.nurseadda.project.dto.response.InvoiceResponse;
import com.nurseadda.project.dto.response.PageResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.dto.response.RateConfigResponse;
import com.nurseadda.project.entity.Invoice;
import com.nurseadda.project.service.InvoicePdfService;
import com.nurseadda.project.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    @PostMapping("/rates")
    public ResponseEntity<RateConfigResponse> createOrUpdateRate(
            Authentication authentication,
            @Valid @RequestBody RateConfigRequest request
    ) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createOrUpdateRateConfig(request, email));
    }

    @DeleteMapping("/rates/{id}")
    public ResponseEntity<String> deleteRate(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = (String) authentication.getPrincipal();
        invoiceService.deleteRateConfig(id, email);
        return ResponseEntity.ok("Rate config deleted successfully");
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
    public ResponseEntity<?> getInvoices(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("");
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("createdAt").descending());
        if (role.equals("ROLE_USER")) {
            return ResponseEntity.ok(invoiceService.getClientInvoices(email, pageable));
        } else {
            return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
        }
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/invoices/{id}/pdf")
    public void downloadInvoicePdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Invoice invoice = invoiceService.getInvoiceEntityById(id);
        ByteArrayOutputStream pdfStream = invoicePdfService.generateInvoicePdf(invoice);

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + String.format("%06d", id) + ".pdf");
        response.getOutputStream().write(pdfStream.toByteArray());
        response.getOutputStream().flush();
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
    public ResponseEntity<?> getPayments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("");
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("createdAt").descending());
        if (role.equals("ROLE_STAFF")) {
            return ResponseEntity.ok(invoiceService.getStaffPayments(email, pageable));
        } else {
            return ResponseEntity.ok(invoiceService.getAllPayments(pageable));
        }
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
