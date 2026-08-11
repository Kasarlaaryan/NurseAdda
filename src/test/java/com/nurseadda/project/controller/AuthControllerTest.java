package com.nurseadda.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurseadda.project.common.exception.GlobalExceptionHandler;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.dto.request.ChangePasswordRequest;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.ForgotPasswordRequest;
import com.nurseadda.project.dto.request.RefreshTokenRequest;
import com.nurseadda.project.dto.request.ResetPasswordRequest;
import com.nurseadda.project.dto.request.SendOtpRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.response.AuthResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =====================================================================
    //  register-staff — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("register-staff: valid request returns 200 with message")
    void registerStaff_validRequest_returns200() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        when(authService.registerStaff(any(StaffRegisterRequest.class)))
                .thenReturn("OTP sent to your email");

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email"));

        verify(authService).registerStaff(any(StaffRegisterRequest.class));
    }

    // =====================================================================
    //  register-staff — NEGATIVE (validation)
    // =====================================================================

    @Test
    @DisplayName("register-staff: blank fullName returns 400 with field error")
    void registerStaff_blankFullName_returns400() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fullName").value("Full name is required"));

        verify(authService, never()).registerStaff(any());
    }

    @Test
    @DisplayName("register-staff: invalid email returns 400 with field error")
    void registerStaff_invalidEmail_returns400() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "not-an-email", "9876543210", "ICU Nurse", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));

        verify(authService, never()).registerStaff(any());
    }

    @Test
    @DisplayName("register-staff: invalid phone returns 400 with field error")
    void registerStaff_invalidPhone_returns400() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "123", "ICU Nurse", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone").value(
                        "Phone number must be 10-15 digits, optionally starting with +"));

        verify(authService, never()).registerStaff(any());
    }

    @Test
    @DisplayName("register-staff: short password returns 400 with field error")
    void registerStaff_shortPassword_returns400() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "123"
        );

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value(
                        "Password must be between 6 and 72 characters"));

        verify(authService, never()).registerStaff(any());
    }

    @Test
    @DisplayName("register-staff: blank staffCategory returns 400 with field error")
    void registerStaff_blankStaffCategory_returns400() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.staffCategory").value("Staff category is required"));

        verify(authService, never()).registerStaff(any());
    }

    @Test
    @DisplayName("register-staff: malformed JSON returns 400")
    void registerStaff_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));

        verify(authService, never()).registerStaff(any());
    }

    @Test
    @DisplayName("register-staff: duplicate email maps to 409 Conflict")
    void registerStaff_duplicateEmail_returns409() throws Exception {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        when(authService.registerStaff(any(StaffRegisterRequest.class)))
                .thenThrow(new UserAlreadyExistException(
                        "User already exists with email : riya@test.com"));

        mockMvc.perform(post("/api/auth/register-staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email : riya@test.com"));
    }

    // =====================================================================
    //  register-client — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("register-client: valid request returns 200 with message")
    void registerClient_validRequest_returns200() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "client@test.com", "9876543210", "secret123"
        );

        when(authService.registerClient(any(ClientRegisterRequest.class)))
                .thenReturn("OTP sent to your email");

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email"));

        verify(authService).registerClient(any(ClientRegisterRequest.class));
    }

    // =====================================================================
    //  register-client — NEGATIVE (validation)
    // =====================================================================

    @Test
    @DisplayName("register-client: blank email returns 400 with field error")
    void registerClient_blankEmail_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "", "9876543210", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email is required"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: invalid email returns 400 with field error")
    void registerClient_invalidEmail_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "bad-email", "9876543210", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: invalid phone returns 400 with field error")
    void registerClient_invalidPhone_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "client@test.com", "abc", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone").value(
                        "Phone number must be 10-15 digits, optionally starting with +"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: short password returns 400 with field error")
    void registerClient_shortPassword_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "client@test.com", "9876543210", "123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value(
                        "Password must be between 6 and 72 characters"));

        verify(authService, never()).registerClient(any());
    }

    // =====================================================================
    //  refresh — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("refresh: valid request returns new tokens")
    void refresh_validRequest_returnsNewTokens() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
        AuthResponseDto response = AuthResponseDto.builder()
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .build();

        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));

        verify(authService).refresh(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("refresh: blank token returns 400 with field error")
    void refresh_blankToken_returns400() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.refreshToken").value("Refresh token is required"));

        verify(authService, never()).refresh(any());
    }

    // =====================================================================
    //  me — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("me: authenticated request returns current user")
    void me_authenticated_returnsCurrentUser() throws Exception {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .email("riya@test.com")
                .build();
        when(authService.getCurrentUser("riya@test.com")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me")
                        .principal(new UsernamePasswordAuthenticationToken("riya@test.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("riya@test.com"));

        verify(authService).getCurrentUser("riya@test.com");
    }

    // =====================================================================
    //  logout — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("logout: revokes access token from header")
    void logout_revokesAccessToken_returns200() throws Exception {
        when(jwtUtil.retrieveTokenFromRequest(any())).thenReturn("access-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(authService).logout(eq("access-token"), any());
    }

    // =====================================================================
    //  resend-otp — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("resend-otp: valid request returns 200 with message")
    void resendOtp_validRequest_returns200() throws Exception {
        SendOtpRequest request = new SendOtpRequest("riya@test.com");

        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email"));

        verify(authService).sendOtp(any(SendOtpRequest.class));
    }

    // =====================================================================
    //  change-password — POSITIVE + NEGATIVE
    // =====================================================================

    @Test
    @DisplayName("change-password: valid request returns 200")
    void changePassword_validRequest_returns200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("old-pass", "new-pass-123");

        mockMvc.perform(post("/api/auth/change-password")
                        .principal(new UsernamePasswordAuthenticationToken("riya@test.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(authService).changePassword(eq("riya@test.com"), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("change-password: short new password returns 400")
    void changePassword_shortNewPassword_returns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("old-pass", "123");

        mockMvc.perform(post("/api/auth/change-password")
                        .principal(new UsernamePasswordAuthenticationToken("riya@test.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPassword").value(
                        "New password must be between 6 and 72 characters"));

        verify(authService, never()).changePassword(any(), any());
    }

    // =====================================================================
    //  forgot-password — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("forgot-password: valid request returns 200")
    void forgotPassword_validRequest_returns200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("riya@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email"));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    // =====================================================================
    //  reset-password — POSITIVE + NEGATIVE (validation)
    // =====================================================================

    @Test
    @DisplayName("reset-password: valid request returns 200")
    void resetPassword_validRequest_returns200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("riya@test.com", "123456", "new-pass-123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully"));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("reset-password: malformed otp returns 400")
    void resetPassword_malformedOtp_returns400() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("riya@test.com", "12ab", "new-pass-123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.otp").value("OTP must be exactly 6 digits"));

        verify(authService, never()).resetPassword(any());
    }

    // =====================================================================
    //  unlock — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("unlock: admin unlocks user account")
    void unlockAccount_validRequest_returns200() throws Exception {
        when(authService.unlockAccount(7L)).thenReturn("Account unlocked successfully");

        mockMvc.perform(patch("/api/auth/users/7/unlock"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account unlocked successfully"));

        verify(authService).unlockAccount(7L);
    }
}
