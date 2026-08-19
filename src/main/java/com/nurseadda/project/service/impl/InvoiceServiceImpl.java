package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.RateConfigRequest;
import com.nurseadda.project.dto.response.BillingSummaryResponse;
import com.nurseadda.project.dto.response.InvoiceResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.dto.response.RateConfigResponse;
import com.nurseadda.project.entity.*;
import com.nurseadda.project.repository.*;
import com.nurseadda.project.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final RateConfigRepository rateConfigRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public RateConfigResponse createOrUpdateRateConfig(RateConfigRequest request) {
        RateConfig rateConfig = rateConfigRepository.findByShiftType(request.getShiftType())
                .orElse(new RateConfig());
        rateConfig.setShiftType(request.getShiftType());
        rateConfig.setStaffHourlyRate(request.getStaffHourlyRate());
        rateConfig.setClientHourlyRate(request.getClientHourlyRate());
        rateConfig.setOvertimeMultiplier(request.getOvertimeMultiplier() != null ? request.getOvertimeMultiplier() : new BigDecimal("1.50"));
        rateConfig = rateConfigRepository.save(rateConfig);
        return mapToRateConfigResponse(rateConfig);
    }

    @Override
    public List<RateConfigResponse> getAllRateConfigs() {
        return rateConfigRepository.findAll().stream().map(this::mapToRateConfigResponse).collect(Collectors.toList());
    }

    @Override
    public RateConfigResponse getRateConfigByShift(String shiftType) {
        RateConfig rc = rateConfigRepository.findByShiftType(shiftType)
                .orElseThrow(() -> new ResourceNotFoundException("Rate config not found for shift: " + shiftType));
        return mapToRateConfigResponse(rc);
    }

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + attendanceId));
        if (attendance.getWorkingHours() == null) throw new IllegalArgumentException("Cannot generate invoice without checkout");

        Assignment assignment = attendance.getAssignment();
        Client client = assignment.getStaffingRequest().getClient();

        if (invoiceRepository.findByAssignmentIdAndClientId(assignment.getId(), client.getId()).isPresent())
            throw new IllegalArgumentException("Invoice already exists for this assignment");

        String shiftType = determineShiftType(assignment.getStaffingRequest().getShift());
        RateConfig rateConfig = rateConfigRepository.findByShiftType(shiftType)
                .orElseThrow(() -> new ResourceNotFoundException("Rate config not found for shift: " + shiftType));

        BigDecimal totalHours = BigDecimal.valueOf(attendance.getWorkingHours());
        BigDecimal threshold = BigDecimal.valueOf(shiftType.equals("12HR") ? 12 : 8);
        BigDecimal baseHours = totalHours.min(threshold);
        BigDecimal overtimeHours = totalHours.subtract(threshold).max(BigDecimal.ZERO);

        BigDecimal baseAmount = baseHours.multiply(rateConfig.getClientHourlyRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal overtimeAmount = overtimeHours.multiply(rateConfig.getClientHourlyRate()).multiply(rateConfig.getOvertimeMultiplier()).setScale(2, RoundingMode.HALF_UP);

        Invoice invoice = new Invoice();
        invoice.setAssignment(assignment);
        invoice.setClient(client);
        invoice.setBaseHours(baseHours);
        invoice.setOvertimeHours(overtimeHours);
        invoice.setClientHourlyRate(rateConfig.getClientHourlyRate());
        invoice.setBaseAmount(baseAmount);
        invoice.setOvertimeAmount(overtimeAmount);
        invoice.setTotalAmount(baseAmount.add(overtimeAmount));
        invoice.setStatus("PENDING");

        return mapToInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public PaymentResponse generatePayment(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + attendanceId));
        if (attendance.getWorkingHours() == null) throw new IllegalArgumentException("Cannot generate payment without checkout");

        Assignment assignment = attendance.getAssignment();
        StaffProfile staffProfile = assignment.getStaffProfile();

        if (paymentRepository.findByAssignmentIdAndStaffProfileId(assignment.getId(), staffProfile.getId()).isPresent())
            throw new IllegalArgumentException("Payment already exists for this assignment");

        String shiftType = determineShiftType(assignment.getStaffingRequest().getShift());
        RateConfig rateConfig = rateConfigRepository.findByShiftType(shiftType)
                .orElseThrow(() -> new ResourceNotFoundException("Rate config not found for shift: " + shiftType));

        BigDecimal totalHours = BigDecimal.valueOf(attendance.getWorkingHours());
        BigDecimal threshold = BigDecimal.valueOf(shiftType.equals("12HR") ? 12 : 8);
        BigDecimal baseHours = totalHours.min(threshold);
        BigDecimal overtimeHours = totalHours.subtract(threshold).max(BigDecimal.ZERO);

        BigDecimal baseAmount = baseHours.multiply(rateConfig.getStaffHourlyRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal overtimeAmount = overtimeHours.multiply(rateConfig.getStaffHourlyRate()).multiply(rateConfig.getOvertimeMultiplier()).setScale(2, RoundingMode.HALF_UP);

        Payment payment = new Payment();
        payment.setAssignment(assignment);
        payment.setStaffProfile(staffProfile);
        payment.setBaseHours(baseHours);
        payment.setOvertimeHours(overtimeHours);
        payment.setStaffHourlyRate(rateConfig.getStaffHourlyRate());
        payment.setBaseAmount(baseAmount);
        payment.setOvertimeAmount(overtimeAmount);
        payment.setTotalAmount(baseAmount.add(overtimeAmount));
        payment.setStatus("PENDING");

        return mapToPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public List<InvoiceResponse> getClientInvoices(String clientEmail) {
        User user = userRepository.findByEmail(clientEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Client client = clientRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        return invoiceRepository.findByClientId(client.getId()).stream().map(this::mapToInvoiceResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(this::mapToInvoiceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoice.setStatus(status);
        return mapToInvoiceResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        return mapToInvoiceResponse(invoiceRepository.findById(invoiceId).orElseThrow(() -> new ResourceNotFoundException("Invoice not found")));
    }

    @Override
    public List<PaymentResponse> getStaffPayments(String staffEmail) {
        User user = userRepository.findByEmail(staffEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        StaffProfile sp = staffProfileRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        return paymentRepository.findByStaffProfileId(sp.getId()).stream().map(this::mapToPaymentResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::mapToPaymentResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(status);
        return mapToPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public BillingSummaryResponse getBillingSummary() {
        BillingSummaryResponse s = new BillingSummaryResponse();
        s.setTotalRevenue(invoiceRepository.getTotalRevenue());
        s.setPendingRevenue(invoiceRepository.getTotalPendingRevenue());
        s.setTotalPaidToStaff(paymentRepository.getTotalPaidOut());
        s.setPendingStaffPayments(paymentRepository.getTotalPendingPayout());
        s.setProfit(s.getTotalRevenue().subtract(s.getTotalPaidToStaff()));
        return s;
    }

    private String determineShiftType(String shift) {
        if (shift == null) return "8HR";
        return shift.toLowerCase().contains("night") || shift.toLowerCase().contains("12") ? "12HR" : "8HR";
    }

    private RateConfigResponse mapToRateConfigResponse(RateConfig c) {
        return new RateConfigResponse(c.getId(), c.getShiftType(), c.getStaffHourlyRate(), c.getClientHourlyRate(), c.getOvertimeMultiplier());
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice i) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(i.getId());
        r.setAssignmentId(i.getAssignment().getId());
        r.setStaffName(i.getAssignment().getStaffProfile().getUser().getFirstName() + " " + i.getAssignment().getStaffProfile().getUser().getLastName());
        r.setDesignation(i.getAssignment().getStaffingRequest().getDesignation());
        r.setLocation(i.getAssignment().getStaffingRequest().getLocation());
        r.setBaseHours(i.getBaseHours());
        r.setOvertimeHours(i.getOvertimeHours());
        r.setClientHourlyRate(i.getClientHourlyRate());
        r.setBaseAmount(i.getBaseAmount());
        r.setOvertimeAmount(i.getOvertimeAmount());
        r.setTotalAmount(i.getTotalAmount());
        r.setStatus(i.getStatus());
        r.setNotes(i.getNotes());
        r.setCreatedAt(i.getCreatedAt());
        return r;
    }

    private PaymentResponse mapToPaymentResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setAssignmentId(p.getAssignment().getId());
        r.setDesignation(p.getAssignment().getStaffingRequest().getDesignation());
        r.setLocation(p.getAssignment().getStaffingRequest().getLocation());
        r.setBaseHours(p.getBaseHours());
        r.setOvertimeHours(p.getOvertimeHours());
        r.setStaffHourlyRate(p.getStaffHourlyRate());
        r.setBaseAmount(p.getBaseAmount());
        r.setOvertimeAmount(p.getOvertimeAmount());
        r.setTotalAmount(p.getTotalAmount());
        r.setStatus(p.getStatus());
        r.setNotes(p.getNotes());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
