package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.UserRequest;
import com.nurseadda.project.dto.request.UserStatusRequest;
import com.nurseadda.project.dto.response.UserResponse;
import com.nurseadda.project.enums.Role;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse createUser(Role callerRole, UserRequest request);

    UserResponse updateUser(Long id, Role callerRole, UserRequest request);

    void deleteUser(Long id);

    UserResponse updateUserStatus(Long id, Role callerRole, UserStatusRequest request);
}
