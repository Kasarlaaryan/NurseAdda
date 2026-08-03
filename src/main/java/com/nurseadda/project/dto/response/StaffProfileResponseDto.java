package com.nurseadda.project.dto.response;

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

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String staffCategory;

    private String aadharCardNumber;

    private LocalDate licenseValidityDate;

    private LocalDate licenseRenewalDate;

    private boolean verified;

    private String stateBoardCertificatePath;

    private List<String> educationalDocumentPaths;

    private List<String> photoPaths;
}
