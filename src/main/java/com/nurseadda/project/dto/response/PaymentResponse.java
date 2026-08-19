package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long assignmentId;
    private String designation;
    private String location;
    private BigDecimal baseHours;
    private BigDecimal overtimeHours;
    private BigDecimal staffHourlyRate;
    private BigDecimal baseAmount;
    private BigDecimal overtimeAmount;
    private BigDecimal totalAmount;
    private String status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String notes;
    private LocalDateTime createdAt;
}
