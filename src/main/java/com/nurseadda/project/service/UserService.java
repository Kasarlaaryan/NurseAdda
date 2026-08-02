package com.nurseadda.project.service;

import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.entity.User;

public interface UserService {

    boolean existsByEmail(String email);

    UserResponseDto save(User user) throws UserAlreadyExistException;
}
