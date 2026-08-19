package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;

import java.util.List;

public interface AssignmentService {

    // Staffing Requests
    StaffingRequestResponse createStaffingRequest(String clientEmail, StaffingRequestDto request);
    List<StaffingRequestResponse> getClientStaffingRequests(String clientEmail);
    List<StaffingRequestResponse> getAllStaffingRequests();
    List<StaffingRequestResponse> getStaffingRequestsByStatus(String status);

    // Assignments
    AssignmentResponse createAssignment(String adminEmail, AssignmentRequest request);
    AssignmentResponse updateAssignmentStatus(String email, Long assignmentId, AssignmentStatusUpdate update, String userRole);
    List<AssignmentResponse> getStaffAssignments(String staffEmail);
    List<AssignmentResponse> getAllAssignments();
    List<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status);
    AssignmentResponse getAssignmentById(Long assignmentId);
}
