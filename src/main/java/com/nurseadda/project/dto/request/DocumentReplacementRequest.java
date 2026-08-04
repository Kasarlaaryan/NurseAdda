package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReplacementRequest {

    @NotNull(message = "Requested status is required")
    private Boolean requested;

    private String reason;

    private LocalDate expiryDate;
}
