package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    private String organizationType;

    private String contactPerson;

    private String address;
}
