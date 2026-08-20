package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffDetailsResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.RequestType;
import com.nurseadda.project.enums.StaffingRequestStatus;

import com.nurseadda.project.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssignmentService {

    // Staffing Requests
    StaffingRequestResponse createStaffingRequest(String clientEmail, StaffingRequestDto request);
    List<StaffingRequestResponse> getClientStaffingRequests(String clientEmail);
    PageResponse<StaffingRequestResponse> getClientStaffingRequests(String clientEmail, Pageable pageable);
    PageResponse<StaffingRequestResponse> getClientStaffingRequests(String clientEmail, RequestType requestType, Pageable pageable);
    List<StaffingRequestResponse> getAllStaffingRequests();
    PageResponse<StaffingRequestResponse> getAllStaffingRequests(Pageable pageable);
    PageResponse<StaffingRequestResponse> getAllStaffingRequests(RequestType requestType, Pageable pageable);
    List<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail);
    PageResponse<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail, Pageable pageable);
    List<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail, RequestType requestType);
    StaffingRequestResponse payAdvance(Long requestId, String clientEmail, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
    List<StaffingRequestResponse> getStaffingRequestsByStatus(String status);
    PageResponse<StaffingRequestResponse> getStaffingRequestsByStatus(String status, Pageable pageable);
    PageResponse<StaffingRequestResponse> getStaffingRequestsByStatus(String status, RequestType requestType, Pageable pageable);
    StaffingRequestResponse updateStaffingRequestStatus(Long requestId, StaffingRequestStatus status, String adminEmail, String userRole);
    List<AssignmentResponse> getStaffingRequestAssignments(Long requestId, String clientEmail);
    PageResponse<AssignmentResponse> getStaffingRequestAssignments(Long requestId, String clientEmail, Pageable pageable);

    // Assignments
    AssignmentResponse createAssignment(String adminEmail, AssignmentRequest request);
    AssignmentResponse acceptStaffingRequest(Long requestId, String staffEmail);
    AssignmentResponse updateAssignmentStatus(String email, Long assignmentId, AssignmentStatusUpdate update, String userRole);
    List<AssignmentResponse> getStaffAssignments(String staffEmail);
    PageResponse<AssignmentResponse> getStaffAssignments(String staffEmail, Pageable pageable);
    List<AssignmentResponse> getAllAssignments();
    PageResponse<AssignmentResponse> getAllAssignments(Pageable pageable);
    PageResponse<AssignmentResponse> getAllAssignments(RequestType requestType, Pageable pageable);
    List<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status, String email, String userRole);
    PageResponse<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status, String email, String userRole, Pageable pageable);
    AssignmentResponse getAssignmentById(Long assignmentId, String email, String userRole);
    StaffDetailsResponse getStaffDetailsForAssignment(Long assignmentId, String email, String userRole);
    AssignmentResponse approveAssignmentToClient(Long assignmentId, String adminEmail, String userRole);
    List<AssignmentResponse> getClientAssignments(String clientEmail);
    PageResponse<AssignmentResponse> getClientAssignments(String clientEmail, Pageable pageable);
    PageResponse<AssignmentResponse> getClientAssignments(String clientEmail, RequestType requestType, Pageable pageable);
}
