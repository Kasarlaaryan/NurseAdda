package com.nurseadda.project.dto.request;

import com.nurseadda.project.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentStatusUpdate {

    @NotNull(message = "Status is required")
    private AssignmentStatus status;

    private String notes;
}
