package com.nurseadda.project.dto;

import com.nurseadda.project.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "Unique user ID", example = "1")
    private Long id;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Assigned role", example = "ROLE_STAFF")
    private Role role;

    @Schema(description = "Whether the account is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Account creation timestamp", example = "2026-07-25T10:30:00")
    private LocalDateTime createdAt;
}
