package com.nurseadda.project.service.impl;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.entity.*;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.repository.*;
import com.nurseadda.project.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final AssignmentRepository assignmentRepository;

    // ─────────────────────────────────────────────
    // Staffing Requests
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public StaffingRequestResponse createStaffingRequest(String clientEmail, StaffingRequestDto request) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        StaffingRequest staffingRequest = new StaffingRequest();
        staffingRequest.setClient(client);
        staffingRequest.setDesignation(request.getDesignation());
        staffingRequest.setLocation(request.getLocation());
        staffingRequest.setShift(request.getShift());
        staffingRequest.setStartDate(request.getStartDate());
        staffingRequest.setEndDate(request.getEndDate());
        staffingRequest.setNumberOfStaff(request.getNumberOfStaff());
        staffingRequest.setRequiredSkills(request.getRequiredSkills());

        staffingRequest = staffingRequestRepository.save(staffingRequest);
        return mapToStaffingRequestResponse(staffingRequest);
    }

    @Override
    public List<StaffingRequestResponse> getClientStaffingRequests(String clientEmail) {
        User user = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + clientEmail));

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found"));

        return staffingRequestRepository.findByClientId(client.getId())
                .stream()
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffingRequestResponse> getAllStaffingRequests() {
        return staffingRequestRepository.findAll()
                .stream()
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffingRequestResponse> getStaffingRequestsByStatus(String status) {
        return staffingRequestRepository.findByStatus(status)
                .stream()
                .map(this::mapToStaffingRequestResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // Assignments
    // ─────────────────────────────────────────────

    @Override
    @Transactional
    public AssignmentResponse createAssignment(String adminEmail, AssignmentRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));

        StaffProfile staffProfile = staffProfileRepository.findById(request.getStaffProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found with id: " + request.getStaffProfileId()));

        // Only verified staff can be assigned
        if (!staffProfile.isVerified()) {
            throw new IllegalArgumentException("Staff member is not verified and cannot be assigned");
        }

        // Check if staff already has an active assignment (no overlapping)
        if (assignmentRepository.hasActiveAssignment(staffProfile.getId())) {
            throw new IllegalArgumentException("Staff member already has an active assignment");
        }

        StaffingRequest staffingRequest = staffingRequestRepository.findById(request.getStaffingRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Staffing request not found with id: " + request.getStaffingRequestId()));

        Assignment assignment = new Assignment();
        assignment.setStaffProfile(staffProfile);
        assignment.setStaffingRequest(staffingRequest);
        assignment.setAssignedBy(admin);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setNotes(request.getNotes());

        assignment = assignmentRepository.save(assignment);
        return mapToAssignmentResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignmentStatus(String email, Long assignmentId, AssignmentStatusUpdate update, String userRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        AssignmentStatus newStatus = update.getStatus();

        // Validate status transitions based on role
        if (userRole.equals(Role.ROLE_STAFF.name())) {
            // Staff can only accept or reject
            if (newStatus != AssignmentStatus.ACCEPTED && newStatus != AssignmentStatus.REJECTED) {
                throw new IllegalArgumentException("Staff can only accept or reject assignments");
            }
            // Verify this assignment belongs to this staff
            StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
            if (!assignment.getStaffProfile().getId().equals(staffProfile.getId())) {
                throw new IllegalArgumentException("This assignment is not assigned to you");
            }
            if (newStatus == AssignmentStatus.ACCEPTED) {
                assignment.setAcceptedAt(LocalDateTime.now());
            }
        } else if (userRole.equals(Role.ROLE_ADMIN.name()) || userRole.equals(Role.ROLE_SUPER_ADMIN.name())) {
            // Admin can set ACTIVE, COMPLETED, or CANCELLED
            if (newStatus != AssignmentStatus.ACTIVE
                    && newStatus != AssignmentStatus.COMPLETED
                    && newStatus != AssignmentStatus.CANCELLED) {
                throw new IllegalArgumentException("Admin can only set status to ACTIVE, COMPLETED, or CANCELLED");
            }
            if (newStatus == AssignmentStatus.COMPLETED) {
                assignment.setCompletedAt(LocalDateTime.now());
            }
        } else {
            throw new IllegalArgumentException("You do not have permission to update assignment status");
        }

        assignment.setStatus(newStatus);
        if (update.getNotes() != null) {
            assignment.setNotes(update.getNotes());
        }

        assignment = assignmentRepository.save(assignment);
        return mapToAssignmentResponse(assignment);
    }

    @Override
    public List<AssignmentResponse> getStaffAssignments(String staffEmail) {
        User user = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + staffEmail));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        return assignmentRepository.findByStaffProfileId(staffProfile.getId())
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByStatus(AssignmentStatus status) {
        return assignmentRepository.findByStatus(status)
                .stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        return mapToAssignmentResponse(assignment);
    }

    // ─────────────────────────────────────────────
    // Mapping helpers
    // ─────────────────────────────────────────────

    private StaffingRequestResponse mapToStaffingRequestResponse(StaffingRequest request) {
        StaffingRequestResponse response = new StaffingRequestResponse();
        response.setId(request.getId());
        response.setClientName(request.getClient().getUser().getFirstName() + " " + request.getClient().getUser().getLastName());
        response.setDesignation(request.getDesignation());
        response.setLocation(request.getLocation());
        response.setShift(request.getShift());
        response.setStartDate(request.getStartDate());
        response.setEndDate(request.getEndDate());
        response.setNumberOfStaff(request.getNumberOfStaff());
        response.setRequiredSkills(request.getRequiredSkills());
        response.setStatus(request.getStatus());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setStaffProfileId(assignment.getStaffProfile().getId());
        response.setStaffName(assignment.getStaffProfile().getUser().getFirstName() + " " + assignment.getStaffProfile().getUser().getLastName());
        response.setStaffEmail(assignment.getStaffProfile().getUser().getEmail());
        response.setStaffCategory(assignment.getStaffProfile().getStaffCategory());
        response.setStaffingRequestId(assignment.getStaffingRequest().getId());
        response.setDesignation(assignment.getStaffingRequest().getDesignation());
        response.setLocation(assignment.getStaffingRequest().getLocation());
        response.setShift(assignment.getStaffingRequest().getShift());
        response.setAssignedByName(assignment.getAssignedBy().getFirstName() + " " + assignment.getAssignedBy().getLastName());
        response.setStatus(assignment.getStatus());
        response.setNotes(assignment.getNotes());
        response.setAcceptedAt(assignment.getAcceptedAt());
        response.setCompletedAt(assignment.getCompletedAt());
        response.setCreatedAt(assignment.getCreatedAt());
        return response;
    }
}
