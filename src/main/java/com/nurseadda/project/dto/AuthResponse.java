package com.nurseadda.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response with JWT token")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Assigned role", example = "ROLE_STAFF")
    private String role;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;
}
