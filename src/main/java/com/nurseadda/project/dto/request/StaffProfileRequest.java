package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.Pattern;
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

    private String location;

    private LocalDate licenseValidityDate;

    private LocalDate licenseRenewalDate;
}
