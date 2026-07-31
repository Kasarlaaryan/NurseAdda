package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;

    private Long userId;

    private String email;

    private String phone;

    private String organizationName;

    private String organizationType;

    private String contactPerson;

    private String address;
}
