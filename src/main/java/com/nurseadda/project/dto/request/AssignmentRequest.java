package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {

    @NotNull(message = "Staff profile ID is required")
    private Long staffProfileId;

    @NotNull(message = "Staffing request ID is required")
    private Long staffingRequestId;

    private String notes;
}
