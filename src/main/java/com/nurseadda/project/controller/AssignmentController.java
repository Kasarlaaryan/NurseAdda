package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffDetailsResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.StaffingRequestStatus;
import com.nurseadda.project.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // ─────────────────────────────────────────────
    // Staffing Requests
    // ─────────────────────────────────────────────

    @PatchMapping("/staffing-requests/{id}/status")
    public ResponseEntity<StaffingRequestResponse> updateStaffingRequestStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam StaffingRequestStatus status
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        StaffingRequestResponse response = assignmentService.updateStaffingRequestStatus(id, status, email, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staffing-requests/{id}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getStaffingRequestAssignments(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = (String) authentication.getPrincipal();
        List<AssignmentResponse> responses = assignmentService.getStaffingRequestAssignments(id, email);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/staffing-requests")
    public ResponseEntity<StaffingRequestResponse> createStaffingRequest(
            Authentication authentication,
            @Valid @RequestBody StaffingRequestDto request
    ) {
        String email = (String) authentication.getPrincipal();
        StaffingRequestResponse response = assignmentService.createStaffingRequest(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/staffing-requests/{id}/pay-advance")
    public ResponseEntity<StaffingRequestResponse> payAdvance(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature
    ) {
        String email = (String) authentication.getPrincipal();
        StaffingRequestResponse response = assignmentService.payAdvance(id, email, razorpayOrderId, razorpayPaymentId, razorpaySignature);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staffing-requests")
    public ResponseEntity<List<StaffingRequestResponse>> getMyStaffingRequests(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");

        List<StaffingRequestResponse> responses;
        if (role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN")) {
            responses = assignmentService.getAllStaffingRequests();
        } else {
            responses = assignmentService.getClientStaffingRequests(email);
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/staffing-requests/status/{status}")
    public ResponseEntity<List<StaffingRequestResponse>> getStaffingRequestsByStatus(
            Authentication authentication,
            @PathVariable String status
    ) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        // FIX 3: Only admin can filter by status
        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<StaffingRequestResponse> responses = assignmentService.getStaffingRequestsByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/staffing-requests/pending")
    public ResponseEntity<List<StaffingRequestResponse>> getPendingRequestsForStaff(
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        // Only staff can see pending requests
        if (!role.equals("ROLE_STAFF")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<StaffingRequestResponse> responses = assignmentService.getPendingRequestsForStaff(email);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/staffing-requests/{id}/accept")
    public ResponseEntity<AssignmentResponse> acceptStaffingRequest(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        // Only staff can accept
        if (!role.equals("ROLE_STAFF")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AssignmentResponse response = assignmentService.acceptStaffingRequest(id, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────
    // Assignments (admin-created)
    // ─────────────────────────────────────────────

    @PostMapping("/assignments")
    public ResponseEntity<AssignmentResponse> createAssignment(
            Authentication authentication,
            @Valid @RequestBody AssignmentRequest request
    ) {
        String email = (String) authentication.getPrincipal();
        AssignmentResponse response = assignmentService.createAssignment(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/assignments/{id}/status")
    public ResponseEntity<AssignmentResponse> updateAssignmentStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AssignmentStatusUpdate update
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        AssignmentResponse response = assignmentService.updateAssignmentStatus(email, id, update, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");

        List<AssignmentResponse> responses;
        if (role.equals("ROLE_STAFF")) {
            responses = assignmentService.getStaffAssignments(email);
        } else if (role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN")) {
            responses = assignmentService.getAllAssignments();
        } else {
            // FIX 4: Client sees their own assignments
            responses = assignmentService.getClientAssignments(email);
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        AssignmentResponse response = assignmentService.getAssignmentById(id, email, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/status/{status}")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByStatus(
            Authentication authentication,
            @PathVariable AssignmentStatus status
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        List<AssignmentResponse> responses = assignmentService.getAssignmentsByStatus(status, email, role);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/assignments/{id}/staff-details")
    public ResponseEntity<StaffDetailsResponse> getStaffDetailsForAssignment(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        StaffDetailsResponse response = assignmentService.getStaffDetailsForAssignment(id, email, role);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/assignments/{id}/approve-to-client")
    public ResponseEntity<AssignmentResponse> approveAssignmentToClient(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        AssignmentResponse response = assignmentService.approveAssignmentToClient(id, email, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/my-clients")
    public ResponseEntity<List<AssignmentResponse>> getMyClientAssignments(
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        List<AssignmentResponse> responses = assignmentService.getClientAssignments(email);
        return ResponseEntity.ok(responses);
    }
}
