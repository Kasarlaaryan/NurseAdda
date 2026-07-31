package com.nurseadda.project.controller;

import com.nurseadda.project.dto.response.DocumentDownload;
import com.nurseadda.project.dto.response.StaffDocumentResponse;
import com.nurseadda.project.entity.DocumentType;
import com.nurseadda.project.service.StaffDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/staff/documents")
@RequiredArgsConstructor
public class StaffDocumentController {

    private final StaffDocumentService staffDocumentService;

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StaffDocumentResponse> upload(
            Authentication authentication,
            @RequestParam DocumentType documentType,
            @RequestParam MultipartFile file) {
        StaffDocumentResponse response =
                staffDocumentService.upload(authentication.getName(), documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<StaffDocumentResponse>> getDocuments(Authentication authentication) {
        return ResponseEntity.ok(staffDocumentService.getDocuments(authentication.getName()));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> download(
            Authentication authentication,
            @PathVariable Long documentId) {
        DocumentDownload download = staffDocumentService.download(authentication.getName(), documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.safeFileName() + "\"")
                .contentType(download.mediaType())
                .body(download.data());
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long documentId) {
        staffDocumentService.delete(authentication.getName(), documentId);
        return ResponseEntity.noContent().build();
    }
}
