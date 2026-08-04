package com.nurseadda.project.dto.request;

import com.nurseadda.project.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must be 10-15 digits, optionally starting with +"
    )
    private String phone;

    @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
    private String password;

    private Role role;

    @Size(max = 100, message = "Staff category must not exceed 100 characters")
    private String staffCategory;
}
