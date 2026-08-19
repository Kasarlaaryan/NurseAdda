package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Mobile number must be 10-15 digits, optionally starting with +"
    )
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @AssertTrue(message = "Passwords do not match")
    private boolean isPasswordsMatch() {
        if (password == null || confirmPassword == null) {
            return true; // null/blank cases are reported by @NotBlank on each field
        }
        return password.equals(confirmPassword);
    }
}
