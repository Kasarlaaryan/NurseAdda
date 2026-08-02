package com.nurseadda.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurseadda.project.common.exception.GlobalExceptionHandler;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

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
}
