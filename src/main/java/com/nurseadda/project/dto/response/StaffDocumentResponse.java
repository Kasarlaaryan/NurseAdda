package com.nurseadda.project.dto.response;

import com.nurseadda.project.entity.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDocumentResponse {

    private Long id;

    private DocumentType documentType;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private LocalDateTime uploadedAt;
}
