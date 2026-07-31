package com.nurseadda.project.service;

import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.response.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthResponse registerStaff(StaffRegisterRequest request);

    AuthResponse registerClient(ClientRegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    AuthResponse getCurrentUser(Authentication authentication);

    /**
     * Admin operation: unlocks a locked account by clearing its failed-login
     * counter and any lock window.
     */
    void unlockUser(Long userId);
}
