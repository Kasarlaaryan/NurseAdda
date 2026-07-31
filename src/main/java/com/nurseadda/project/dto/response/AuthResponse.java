package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String email;

    private String role;

    private String firstName;

    private String lastName;

    /** Only populated for ROLE_STAFF accounts. */
    private String staffCategory;

    /** Whether the staff profile has been approved by an admin (null for non-staff). */
    private Boolean verified;
}
