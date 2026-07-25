package com.nurseadda.project.service;

import com.nurseadda.project.dto.UserRequest;
import com.nurseadda.project.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);
}
