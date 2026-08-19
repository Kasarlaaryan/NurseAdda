package com.nurseadda.project.repository;

import com.nurseadda.project.entity.Assignment;
import com.nurseadda.project.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByStaffProfileId(Long staffProfileId);

    List<Assignment> findByStaffingRequestId(Long staffingRequestId);

    List<Assignment> findByStatus(AssignmentStatus status);

    List<Assignment> findByStaffProfileIdAndStatus(Long staffProfileId, AssignmentStatus status);

    List<Assignment> findByStaffingRequestIdAndStatus(Long staffingRequestId, AssignmentStatus status);

    List<Assignment> findByAssignedById(Long adminId);

    @Query("SELECT a FROM Assignment a WHERE a.staffProfile.id = :staffProfileId " +
            "AND a.status IN ('PENDING', 'ACCEPTED', 'ACTIVE')")
    List<Assignment> findActiveAssignmentsByStaff(@Param("staffProfileId") Long staffProfileId);

    @Query("SELECT COUNT(a) > 0 FROM Assignment a WHERE a.staffProfile.id = :staffProfileId " +
            "AND a.status IN ('PENDING', 'ACCEPTED', 'ACTIVE')")
    boolean hasActiveAssignment(@Param("staffProfileId") Long staffProfileId);

    List<Assignment> findByStaffingRequestIdAndStaffProfileId(Long staffingRequestId, Long staffProfileId);
}
