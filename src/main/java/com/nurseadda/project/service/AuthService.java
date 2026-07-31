package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.RegisterRequest;
import com.nurseadda.project.dto.response.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    AuthResponse getCurrentUser(Authentication authentication);
}
