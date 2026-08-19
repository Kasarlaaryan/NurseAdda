package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffingRequestDto {

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Shift is required")
    private String shift;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Min(value = 1, message = "At least 1 staff member is required")
    private int numberOfStaff;

    private String requiredSkills;
}
