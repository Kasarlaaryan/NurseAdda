package com.nurseadda.project.dto.response;

import com.nurseadda.project.enums.StaffDocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDocumentResponseDto {

    private Long id;

    private StaffDocumentType documentType;

    private String fileName;

    private String filePath;

    private boolean verified;

    private LocalDate expiryDate;

    private boolean replacementRequested;

    private String requestReason;
}
