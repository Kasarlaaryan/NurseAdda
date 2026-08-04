package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateStaffProfileRequest {

    @Size(max = 100, message = "Staff category must not exceed 100 characters")
    private String staffCategory;

    @Pattern(
            regexp = "^[0-9]{12}$",
            message = "Aadhar card number must be exactly 12 digits"
    )
    private String aadharCardNumber;

    private LocalDate licenseValidityDate;

    private LocalDate licenseRenewalDate;
}
