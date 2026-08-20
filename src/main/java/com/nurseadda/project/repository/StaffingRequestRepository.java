package com.nurseadda.project.repository;

import com.nurseadda.project.entity.StaffingRequest;
import com.nurseadda.project.enums.RequestType;
import com.nurseadda.project.enums.StaffingRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffingRequestRepository extends JpaRepository<StaffingRequest, Long> {
    List<StaffingRequest> findByClientId(Long clientId);
    Page<StaffingRequest> findByClientId(Long clientId, Pageable pageable);
    Page<StaffingRequest> findByClientIdAndRequestType(Long clientId, RequestType requestType, Pageable pageable);
    List<StaffingRequest> findByStatus(StaffingRequestStatus status);
    Page<StaffingRequest> findByStatus(StaffingRequestStatus status, Pageable pageable);
    Page<StaffingRequest> findByStatusAndRequestType(StaffingRequestStatus status, RequestType requestType, Pageable pageable);
}
