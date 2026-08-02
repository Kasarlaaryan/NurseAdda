package com.nurseadda.project.dto.request;

import com.nurseadda.project.enums.Qualification;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffProfileRequest {

    @Pattern(
            regexp = "^[0-9]{12}$",
            message = "Aadhar card number must be exactly 12 digits"
    )
    private String aadharCardNumber;

    private Qualification qualification;

    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;

    private LocalDate licenseExpiryDate;

    private Integer yearsOfExperience;

    @Pattern(
            regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$",
            message = "PAN card number must be in format ABCDE1234F"
    )
    private String panCardNumber;
}
