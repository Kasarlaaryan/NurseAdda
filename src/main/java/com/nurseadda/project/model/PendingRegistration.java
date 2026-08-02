package com.nurseadda.project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingRegistration {

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String phone;

    private String role;

    private String staffCategory;

    private String code;

    private LocalDateTime expiresAt;

    private boolean verified;
}
