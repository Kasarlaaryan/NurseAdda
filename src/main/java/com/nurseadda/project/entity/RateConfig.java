package com.nurseadda.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rate_configs")
public class RateConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String shiftType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal staffHourlyRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal clientHourlyRate;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeMultiplier = new BigDecimal("1.50");

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
