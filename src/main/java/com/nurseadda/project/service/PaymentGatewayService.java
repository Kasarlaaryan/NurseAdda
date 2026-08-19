package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.CreateOrderRequest;
import com.nurseadda.project.dto.request.VerifyPaymentRequest;
import com.nurseadda.project.dto.response.CreateOrderResponse;
import com.nurseadda.project.dto.response.PaymentResponse;

public interface PaymentGatewayService {
    CreateOrderResponse createOrder(CreateOrderRequest request);
    PaymentResponse verifyPayment(VerifyPaymentRequest request);
    boolean isAvailable();
}
