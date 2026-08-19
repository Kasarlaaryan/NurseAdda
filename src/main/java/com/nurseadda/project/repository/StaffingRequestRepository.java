package com.nurseadda.project.repository;

import com.nurseadda.project.entity.StaffingRequest;
import com.nurseadda.project.enums.StaffingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {
    List<StaffingRequest> findByClientId(Long clientId);
    List<StaffingRequest> findByStatus(StaffingRequestStatus status);
}
