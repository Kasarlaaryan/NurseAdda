package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffDetailsResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.StaffingRequestStatus;

import java.util.List;

public interface AssignmentService {

    // Staffing Requests
    StaffingRequestResponse createStaffingRequest(String clientEmail, StaffingRequestDto request);
    List<StaffingRequestResponse> getClientStaffingRequests(String clientEmail);
    List<StaffingRequestResponse> getAllStaffingRequests();
    List<StaffingRequestResponse> getPendingRequestsForStaff(String staffEmail);
    StaffingRequestResponse payAdvance(Long requestId, String clientEmail, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
    List<StaffingRequestResponse> getStaffingRequestsByStatus(String status);
    StaffingRequestResponse updateStaffingRequestStatus(Long requestId, StaffingRequestStatus status, String adminEmail, String userRole);
    List<AssignmentResponse> getStaffingRequestAssignments(Long requestId, String clientEmail);

    // Assignments
    AssignmentResponse createAssignment(String adminEmail, AssignmentRequest request);
    AssignmentResponse acceptStaffingRequest(Long requestId, String staffEmail);
    AssignmentResponse updateAssignmentStatus(String email, Long assignmentId, AssignmentStatusUpdate update, String userRole);
    List<AssignmentResponse> getStaffAssignments(String staffEmail);
    List<AssignmentResponse> getAllAssignments();
    List<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status, String email, String userRole);
    AssignmentResponse getAssignmentById(Long assignmentId, String email, String userRole);
    StaffDetailsResponse getStaffDetailsForAssignment(Long assignmentId, String email, String userRole);
    AssignmentResponse approveAssignmentToClient(Long assignmentId, String adminEmail, String userRole);
    List<AssignmentResponse> getClientAssignments(String clientEmail);
}
