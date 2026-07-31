package com.nurseadda.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "staff_profiles")
public class StaffProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "staff_category")
    private String staffCategory;

    // ---- Professional & identification ----

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "nursing_council_reg_number")
    private String nursingCouncilRegNumber;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "specializations")
    private String specializations;

    // ---- Operational & logistics ----

    @Column(name = "current_address")
    private String currentAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.ACTIVE;

    // ---- Bank details ----

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
