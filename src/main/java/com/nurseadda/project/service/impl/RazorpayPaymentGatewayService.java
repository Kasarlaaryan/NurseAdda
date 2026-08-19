package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.CreateOrderRequest;
import com.nurseadda.project.dto.request.VerifyPaymentRequest;
import com.nurseadda.project.dto.response.CreateOrderResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.entity.Payment;
import com.nurseadda.project.repository.PaymentRepository;
import com.nurseadda.project.service.PaymentGatewayService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayPaymentGatewayService implements PaymentGatewayService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Override
    public boolean isAvailable() {
        return razorpayClient != null && keyId != null && !keyId.isBlank();
    }

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("Payment gateway is not configured");
        }

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));

        if ("PAID".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Payment is already completed");
        }

        try {
            // Razorpay expects amount in paise (smallest currency unit)
            BigDecimal amountInPaise = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise.intValue());
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            orderRequest.put("receipt", "payment_" + payment.getId());
            orderRequest.put("notes", new JSONObject()
                    .put("paymentId", payment.getId())
                    .put("assignmentId", payment.getAssignment().getId()));

            com.razorpay.Order order = razorpayClient.orders.create(orderRequest);

            log.info("Razorpay order created: {} for payment {}", order.get("id"), payment.getId());

            return new CreateOrderResponse(
                    order.get("id").toString(),
                    request.getAmount(),
                    request.getCurrency() != null ? request.getCurrency() : "INR",
                    keyId
            );
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("Payment gateway is not configured");
        }

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + request.getPaymentId()));

        // Verify signature
        if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            throw new IllegalArgumentException("Invalid payment signature - payment verification failed");
        }

        // Update payment record
        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus("PAID");

        payment = paymentRepository.save(payment);
        log.info("Payment {} verified and marked as PAID", payment.getId());

        return mapToPaymentResponse(payment);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes());
            String expectedSignature = Base64.getEncoder().encodeToString(digest);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
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
        r.setRazorpayOrderId(p.getRazorpayOrderId());
        r.setRazorpayPaymentId(p.getRazorpayPaymentId());
        r.setNotes(p.getNotes());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
