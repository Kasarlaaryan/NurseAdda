package com.nurseadda.project.repository;

import com.nurseadda.project.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByClientId(Long clientId);
    Page<Invoice> findByClientId(Long clientId, Pageable pageable);
    List<Invoice> findByAssignmentId(Long assignmentId);
    List<Invoice> findByStatus(String status);
    Optional<Invoice> findByAssignmentIdAndClientId(Long assignmentId, Long clientId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.client.id = :clientId AND i.status = :status")
    BigDecimal getTotalByClientAndStatus(@Param("clientId") Long clientId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PENDING'")
    BigDecimal getTotalPendingRevenue();
}
