package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDocumentVerificationRequest {

    @NotNull(message = "Verified status is required")
    private Boolean verified;
}
