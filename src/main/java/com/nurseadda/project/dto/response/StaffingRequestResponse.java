package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffingRequestResponse {

    private Long id;
    private String clientName;
    private String designation;
    private String location;
    private String shift;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfStaff;
    private String requiredSkills;
    private String status;
    private LocalDateTime createdAt;
}
