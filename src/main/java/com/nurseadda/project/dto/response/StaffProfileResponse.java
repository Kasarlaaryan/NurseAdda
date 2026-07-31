package com.nurseadda.project.dto.response;

import com.nurseadda.project.entity.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffProfileResponse {

    private Long id;

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String staffCategory;

    private String aadharNumber;

    private String nursingCouncilRegNumber;

    private Integer yearsOfExperience;

    private String specializations;

    private String currentAddress;

    private AvailabilityStatus availabilityStatus;

    private String bankAccountNumber;

    private String bankName;

    private String ifscCode;

    private boolean verified;
}
