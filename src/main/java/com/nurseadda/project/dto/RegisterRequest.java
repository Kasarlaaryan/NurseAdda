package com.nurseadda.project.dto;

import com.nurseadda.project.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "New user registration request")
public class RegisterRequest {

    @Schema(description = "Email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Password (min 6 characters)", example = "password123")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
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
    @NotNull(message = "Role is required")
    private Role role;
}
