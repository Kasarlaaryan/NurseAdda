package com.nurseadda.project.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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

    @Column(nullable = false, length = 100)
    private String staffCategory;

    @Column(length = 200)
    private String location;

    @Column(length = 12)
    private String aadharCardNumber;

    private LocalDate licenseValidityDate;

    private LocalDate licenseRenewalDate;

    private boolean verified = false;
}
