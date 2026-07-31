package com.nurseadda.project.controller;

import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.request.StaffVerificationRequest;
import com.nurseadda.project.dto.response.DocumentDownload;
import com.nurseadda.project.dto.response.StaffDocumentResponse;
import com.nurseadda.project.dto.response.StaffProfileResponse;
import com.nurseadda.project.service.StaffDocumentService;
import com.nurseadda.project.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final StaffDocumentService staffDocumentService;

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/profile")
    public ResponseEntity<StaffProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody StaffProfileRequest request) {
        return ResponseEntity.ok(staffService.updateProfile(authentication.getName(), request));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/profile")
    public ResponseEntity<StaffProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(staffService.getProfile(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PatchMapping("/{userId}/verification")
    public ResponseEntity<StaffProfileResponse> verifyProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StaffVerificationRequest request) {
        return ResponseEntity.ok(staffService.verifyProfile(userId, request.getVerified()));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/{userId}/documents")
    public ResponseEntity<List<StaffDocumentResponse>> getDocuments(@PathVariable Long userId) {
        return ResponseEntity.ok(staffDocumentService.getDocumentsByUserId(userId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/{userId}/documents/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long userId,
            @PathVariable Long documentId) {
        DocumentDownload download = staffDocumentService.downloadByUserId(userId, documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.safeFileName() + "\"")
                .contentType(download.mediaType())
                .body(download.data());
    }
}
