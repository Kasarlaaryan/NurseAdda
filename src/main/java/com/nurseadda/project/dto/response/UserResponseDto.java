package com.nurseadda.project.dto.response;

import com.nurseadda.project.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private Role role;
}
