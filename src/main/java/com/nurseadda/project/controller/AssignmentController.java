package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.PageResponse;
import com.nurseadda.project.dto.response.StaffDetailsResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;
import com.nurseadda.project.enums.RequestType;
import com.nurseadda.project.enums.StaffingRequestStatus;
import com.nurseadda.project.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<?> getMyStaffingRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RequestType requestType
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("createdAt").descending());

        if (role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN")) {
            return ResponseEntity.ok(assignmentService.getAllStaffingRequests(requestType, pageable));
        } else {
            return ResponseEntity.ok(assignmentService.getClientStaffingRequests(email, requestType, pageable));
        }
    }

    @GetMapping("/staffing-requests/status/{status}")
    public ResponseEntity<?> getStaffingRequestsByStatus(
            Authentication authentication,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RequestType requestType
    ) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        return ResponseEntity.ok(assignmentService.getStaffingRequestsByStatus(status, requestType, pageable));
    }

    @GetMapping("/staffing-requests/pending")
    public ResponseEntity<?> getPendingRequestsForStaff(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RequestType requestType
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        if (!role.equals("ROLE_STAFF")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(assignmentService.getPendingRequestsForStaff(email, requestType));
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
    public ResponseEntity<?> getAssignments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RequestType requestType
    ) {
        String email = (String) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("createdAt").descending());

        if (role.equals("ROLE_STAFF")) {
            return ResponseEntity.ok(assignmentService.getStaffAssignments(email, pageable));
        } else if (role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN")) {
            return ResponseEntity.ok(assignmentService.getAllAssignments(requestType, pageable));
        } else {
            return ResponseEntity.ok(assignmentService.getClientAssignments(email, requestType, pageable));
        }
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
    public ResponseEntity<?> getMyClientAssignments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String email = (String) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        return ResponseEntity.ok(assignmentService.getClientAssignments(email, pageable));
    }
}
