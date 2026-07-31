package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.BadRequestException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.response.DocumentDownload;
import com.nurseadda.project.dto.response.StaffDocumentResponse;
import com.nurseadda.project.entity.DocumentType;
import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.StaffDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffDocumentServiceImpl implements StaffDocumentService {

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StaffDocumentRepository staffDocumentRepository;

    @Override
    @Transactional
    public StaffDocumentResponse upload(String email, DocumentType documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String contentType = file.getContentType();
        boolean allowed = contentType != null
                && (contentType.equals("application/pdf")
                    || contentType.startsWith("image/"));
        if (!allowed) {
            throw new BadRequestException(
                    "Only PDF or image files are allowed");
        }

        StaffProfile profile = findProfileByEmail(email);

        try {
            StaffDocument document = new StaffDocument();
            document.setStaffProfile(profile);
            document.setDocumentType(documentType);
            document.setFileName(resolveFileName(file));
            document.setContentType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setFileData(file.getBytes());

            staffDocumentRepository.save(document);

            return toResponse(document);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read uploaded file");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffDocumentResponse> getDocuments(String email) {
        StaffProfile profile = findProfileByEmail(email);
        return staffDocumentRepository.findByStaffProfileId(profile.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload download(String email, Long documentId) {
        StaffProfile profile = findProfileByEmail(email);
        StaffDocument document = staffDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("StaffDocument", documentId));

        if (!document.getStaffProfile().getId().equals(profile.getId())) {
            throw new ResourceNotFoundException("StaffDocument", documentId);
        }

        return new DocumentDownload(
                document.getFileData(),
                document.getContentType(),
                document.getFileName());
    }

    @Override
    @Transactional
    public void delete(String email, Long documentId) {
        StaffProfile profile = findProfileByEmail(email);
        StaffDocument document = staffDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("StaffDocument", documentId));

        if (!document.getStaffProfile().getId().equals(profile.getId())) {
            throw new ResourceNotFoundException("StaffDocument", documentId);
        }

        staffDocumentRepository.delete(document);
    }

    private String resolveFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        return (original == null || original.isBlank()) ? "document" : original;
    }

    private StaffProfile findProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("StaffProfile", "user", email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffDocumentResponse> getDocumentsByUserId(Long userId) {
        return staffDocumentRepository.findByStaffProfileUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload downloadByUserId(Long userId, Long documentId) {
        StaffDocument document = staffDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("StaffDocument", documentId));

        if (!document.getStaffProfile().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("StaffDocument", documentId);
        }

        return new DocumentDownload(
                document.getFileData(),
                document.getContentType(),
                document.getFileName());
    }

    private StaffDocumentResponse toResponse(StaffDocument document) {
        return StaffDocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
