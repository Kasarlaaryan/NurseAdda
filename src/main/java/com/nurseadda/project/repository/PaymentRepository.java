package com.nurseadda.project.repository;

import com.nurseadda.project.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStaffProfileId(Long staffProfileId);
    List<Payment> findByAssignmentId(Long assignmentId);
    List<Payment> findByStatus(String status);
    Optional<Payment> findByAssignmentIdAndStaffProfileId(Long assignmentId, Long staffProfileId);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payment p WHERE p.staffProfile.id = :staffProfileId AND p.status = :status")
    BigDecimal getTotalByStaffAndStatus(@Param("staffProfileId") Long staffProfileId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal getTotalPaidOut();

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payment p WHERE p.status = 'PENDING'")
    BigDecimal getTotalPendingPayout();
}
