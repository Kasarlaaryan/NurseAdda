package com.nurseadda.project.entity;

import com.nurseadda.project.enums.Qualification;
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

    @Column(length = 12)
    private String aadharCardNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Qualification qualification;

    @Column(length = 100)
    private String licenseNumber;

    private LocalDate licenseExpiryDate;

    private Integer yearsOfExperience;

    @Column(length = 10)
    private String panCardNumber;

    private boolean verified = false;
}
