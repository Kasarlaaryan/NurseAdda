package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Size(max = 72, message = "Current password must not exceed 72 characters")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 72, message = "New password must be between 6 and 72 characters")
    private String newPassword;
}
