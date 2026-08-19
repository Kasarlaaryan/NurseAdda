package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetailsResponse {

    // Assignment info
    private Long assignmentId;
    private String assignmentStatus;
    private String notes;
    private boolean sentToClient;
    private LocalDateTime sentToClientAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;

    // Staff profile info
    private Long staffProfileId;
    private String staffName;
    private String staffEmail;
    private String staffPhone;
    private String staffCategory;
    private boolean verified;

    // Staffing request info
    private Long staffingRequestId;
    private String designation;
    private String location;
    private String shift;

    // Staff documents
    private List<StaffDocumentInfo> documents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffDocumentInfo {
        private Long id;
        private String documentType;
        private String filePath;
        private String fileName;
    }
}
