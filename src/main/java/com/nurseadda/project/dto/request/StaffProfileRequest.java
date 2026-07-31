package com.nurseadda.project.dto.request;

import com.nurseadda.project.entity.AvailabilityStatus;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffProfileRequest {

    private String aadharNumber;

    private String nursingCouncilRegNumber;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    private String specializations;

    private String currentAddress;

    private AvailabilityStatus availabilityStatus;

    private String bankAccountNumber;

    private String bankName;

    private String ifscCode;
}
