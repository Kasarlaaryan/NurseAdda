package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.AssignmentRequest;
import com.nurseadda.project.dto.request.AssignmentStatusUpdate;
import com.nurseadda.project.dto.request.StaffingRequestDto;
import com.nurseadda.project.dto.response.AssignmentResponse;
import com.nurseadda.project.dto.response.StaffingRequestResponse;
import com.nurseadda.project.enums.AssignmentStatus;
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

    @PostMapping("/staffing-requests")
    public ResponseEntity<StaffingRequestResponse> createStaffingRequest(
            Authentication authentication,
            @Valid @RequestBody StaffingRequestDto request
    ) {
        String email = (String) authentication.getPrincipal();
        StaffingRequestResponse response = assignmentService.createStaffingRequest(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            @PathVariable String status
    ) {
        List<StaffingRequestResponse> responses = assignmentService.getStaffingRequestsByStatus(status);
        return ResponseEntity.ok(responses);
    }

    // ─────────────────────────────────────────────
    // Assignments
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
            responses = List.of(); // clients see empty for now
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/status/{status}")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByStatus(
            @PathVariable AssignmentStatus status
    ) {
        List<AssignmentResponse> responses = assignmentService.getAssignmentsByStatus(status);
        return ResponseEntity.ok(responses);
    }
}
