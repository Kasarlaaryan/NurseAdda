package com.nurseadda.project.dto.response;

import com.nurseadda.project.enums.Qualification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffProfileResponseDto {

    private Long id;

    private String staffCategory;

    private String aadharCardNumber;

    private Qualification qualification;

    private String licenseNumber;

    private LocalDate licenseExpiryDate;

    private Integer yearsOfExperience;

    private String panCardNumber;

    private boolean verified;

    private String passportPhotoPath;

    private List<String> educationalDocumentPaths;
}
