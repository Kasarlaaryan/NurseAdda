package com.nurseadda.project.service;

import com.nurseadda.project.dto.response.DocumentDownload;
import com.nurseadda.project.dto.response.StaffDocumentResponse;
import com.nurseadda.project.entity.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StaffDocumentService {

    StaffDocumentResponse upload(String email, DocumentType documentType, MultipartFile file);

    List<StaffDocumentResponse> getDocuments(String email);

    DocumentDownload download(String email, Long documentId);

    void delete(String email, Long documentId);

    /** Admin view: all documents of a given staff user. */
    List<StaffDocumentResponse> getDocumentsByUserId(Long userId);

    /** Admin view: download one document of a given staff user. */
    DocumentDownload downloadByUserId(Long userId, Long documentId);
}
