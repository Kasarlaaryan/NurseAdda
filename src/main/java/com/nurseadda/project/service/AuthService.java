package com.nurseadda.project.service;

import com.nurseadda.project.dto.AuthResponse;
import com.nurseadda.project.dto.LoginRequest;
import com.nurseadda.project.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
