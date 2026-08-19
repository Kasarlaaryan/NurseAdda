package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.CreateOrderRequest;
import com.nurseadda.project.dto.request.VerifyPaymentRequest;
import com.nurseadda.project.dto.response.CreateOrderResponse;
import com.nurseadda.project.dto.response.PaymentResponse;
import com.nurseadda.project.service.PaymentGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment-gateway")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        if (!paymentGatewayService.isAvailable()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Payment gateway is not configured"));
        }
        CreateOrderResponse response = paymentGatewayService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentGatewayService.verifyPayment(request));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getGatewayStatus() {
        return ResponseEntity.ok(Map.of("available", paymentGatewayService.isAvailable()));
    }
}
