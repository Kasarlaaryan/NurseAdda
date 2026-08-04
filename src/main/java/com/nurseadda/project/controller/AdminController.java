package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.AdminCreateUserRequest;
import com.nurseadda.project.dto.request.AdminUpdateStaffProfileRequest;
import com.nurseadda.project.dto.request.AdminUpdateUserRequest;
import com.nurseadda.project.dto.request.DocumentReplacementRequest;
import com.nurseadda.project.dto.request.StaffDocumentVerificationRequest;
import com.nurseadda.project.dto.request.StaffVerificationRequest;
import com.nurseadda.project.dto.request.UserStatusRequest;
import com.nurseadda.project.dto.response.StaffDocumentResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // =====================================================================
    //  Users
    // =====================================================================

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Role role
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(clampPageable(page, size), role));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        String actingEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(adminService.updateUser(id, request, actingEmail));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(Authentication authentication, @PathVariable Long id) {
        String actingEmail = (String) authentication.getPrincipal();
        adminService.deleteUser(id, actingEmail);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        String actingEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(adminService.setUserEnabled(id, request.getEnabled(), actingEmail));
    }

    // =====================================================================
    //  Staff
    // =====================================================================

    @GetMapping("/staff")
    public ResponseEntity<Page<StaffProfileResponseDto>> getAllStaffProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAllStaffProfiles(clampPageable(page, size)));
    }

    @GetMapping("/staff/{userId}")
    public ResponseEntity<StaffProfileResponseDto> getStaffProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getStaffProfileByUserId(userId));
    }

    @PutMapping("/staff/{userId}")
    public ResponseEntity<StaffProfileResponseDto> updateStaffProfile(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateStaffProfileRequest request
    ) {
        return ResponseEntity.ok(adminService.updateStaffProfileByAdmin(userId, request));
    }

    @PatchMapping("/staff/{userId}/verification")
    public ResponseEntity<StaffProfileResponseDto> verifyStaffProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StaffVerificationRequest request
    ) {
        return ResponseEntity.ok(adminService.verifyStaffProfile(userId, request.getVerified()));
    }

    // =====================================================================
    //  Staff documents
    // =====================================================================

    @GetMapping("/staff/{userId}/documents")
    public ResponseEntity<List<StaffDocumentResponseDto>> getStaffDocuments(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getStaffDocuments(userId));
    }

    @PatchMapping("/staff/{userId}/documents/{documentId}/verification")
    public ResponseEntity<StaffDocumentResponseDto> verifyStaffDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId,
            @Valid @RequestBody StaffDocumentVerificationRequest request
    ) {
        return ResponseEntity.ok(
                adminService.verifyStaffDocument(userId, documentId, request.getVerified())
        );
    }

    @PatchMapping("/staff/{userId}/documents/{documentId}/replacement-request")
    public ResponseEntity<StaffDocumentResponseDto> requestDocumentReplacement(
            @PathVariable Long userId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentReplacementRequest request
    ) {
        return ResponseEntity.ok(
                adminService.requestDocumentReplacement(
                        userId,
                        documentId,
                        request.getRequested(),
                        request.getReason(),
                        request.getExpiryDate()
                )
        );
    }

    private PageRequest clampPageable(int page, int size) {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        return PageRequest.of(page, size);
    }
}
