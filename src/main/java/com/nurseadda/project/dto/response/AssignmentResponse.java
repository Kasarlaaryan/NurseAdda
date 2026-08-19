package com.nurseadda.project.dto.response;

import com.nurseadda.project.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private Long staffProfileId;
    private String staffName;
    private String staffEmail;
    private String staffCategory;
    private Long staffingRequestId;
    private String designation;
    private String location;
    private String shift;
    private String assignedByName;
    private AssignmentStatus status;
    private String notes;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
