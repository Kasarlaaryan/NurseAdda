package com.nurseadda.project.dto;

import com.nurseadda.project.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User creation / update request")
public class UserRequest {

    @Schema(description = "Email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Password (leave blank if not changing)", example = "newPassword123")
    private String password;

    @Schema(description = "First name", example = "John")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "User role", example = "ROLE_STAFF", allowableValues = {"ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_STAFF", "ROLE_CLIENT"})
    private Role role;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private Boolean enabled;
}
