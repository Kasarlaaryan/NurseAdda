package com.nurseadda.project.entity;

import com.nurseadda.project.enums.RequestType;
import com.nurseadda.project.enums.StaffingRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "staffing_requests")
public class StaffingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 100)
    private String designation;

    @Column(nullable = false, length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestType requestType = RequestType.ON_CALL;

    @Column(nullable = false, length = 50)
    private String shift;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int numberOfStaff;

    @Column(length = 500)
    private String requiredSkills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffingRequestStatus status = StaffingRequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime deadline;

    // Advance payment fields (40% upfront)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal advanceAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean advancePaid = false;

    @Column(length = 100)
    private String advanceRazorpayOrderId;

    @Column(length = 100)
    private String advanceRazorpayPaymentId;

    @Column(length = 256)
    private String advanceRazorpaySignature;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
