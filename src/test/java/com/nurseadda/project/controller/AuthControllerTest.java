package com.nurseadda.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nurseadda.project.common.exception.GlobalExceptionHandler;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
import com.nurseadda.project.dto.request.ClientProfileRequest;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.request.StaffVerificationRequest;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

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
                "Client", "User", "client@test.com", "9876543210", "secret123", "secret123"
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
                "Client", "User", "", "9876543210", "secret123", "secret123"
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
                "Client", "User", "bad-email", "9876543210", "secret123", "secret123"
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
                "Client", "User", "client@test.com", "abc", "secret123", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mobileNumber").value(
                        "Mobile number must be 10-15 digits, optionally starting with +"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: short password returns 400 with field error")
    void registerClient_shortPassword_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "123", "123"
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
    //  register-client — NEW FIELDS (firstName / lastName / mobileNumber / confirmPassword)
    // =====================================================================

    @Test
    @DisplayName("register-client: blank firstName returns 400 with field error")
    void registerClient_blankFirstName_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "", "User", "client@test.com", "9876543210", "secret123", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: blank lastName returns 400 with field error")
    void registerClient_blankLastName_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "", "client@test.com", "9876543210", "secret123", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Last name is required"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: blank mobileNumber returns 400 with field error")
    void registerClient_blankMobileNumber_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", null, "secret123", "secret123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mobileNumber").value("Mobile number is required"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: blank confirmPassword returns 400 with field error")
    void registerClient_blankConfirmPassword_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "secret123", ""
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.confirmPassword").value("Confirm password is required"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    @DisplayName("register-client: mismatched passwords returns 400 with field error")
    void registerClient_mismatchedPasswords_returns400() throws Exception {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "secret123", "different123"
        );

        mockMvc.perform(post("/api/auth/register-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.passwordsMatch").value("Passwords do not match"));

        verify(authService, never()).registerClient(any());
    }

    // =====================================================================
    //  updateStaffProfile (PUT /api/auth/staff-profile - multipart)
    // =====================================================================

    @Test
    @DisplayName("updateStaffProfile: valid multipart request returns 200 with updated profile")
    void updateStaffProfile_validRequest_returns200() throws Exception {
        StaffProfileRequest request = new StaffProfileRequest(
                "123456789012", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 15));

        StaffProfileResponseDto responseDto = StaffProfileResponseDto.builder()
                .id(10L)
                .staffCategory("ICU Nurse")
                .aadharCardNumber("123456789012")
                .licenseValidityDate(LocalDate.of(2026, 12, 31))
                .licenseRenewalDate(LocalDate.of(2026, 1, 15))
                .stateBoardCertificatePath("uploads/staff/5/certificate/cert.pdf")
                .educationalDocumentPaths(java.util.List.of("uploads/staff/5/education/bsc.pdf"))
                .build();

        when(authService.updateStaffProfile(eq("rohan@test.com"), any(StaffProfileRequest.class),
                any(MultipartFile.class), any(), any())).thenReturn(responseDto);

        MockMultipartFile profile = new MockMultipartFile("profile", "profile.json",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        MockMultipartFile cert = new MockMultipartFile("stateBoardCertificate", "cert.pdf",
                MediaType.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3});
        MockMultipartFile photo = new MockMultipartFile("photos", "staff-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE, new byte[]{4, 5, 6});

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/auth/staff-profile")
                        .file(profile)
                        .file(cert)
                        .file(photo)
                        .principal(new UsernamePasswordAuthenticationToken("rohan@test.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aadharCardNumber").value("123456789012"))
                .andExpect(jsonPath("$.stateBoardCertificatePath").value("uploads/staff/5/certificate/cert.pdf"));

        verify(authService).updateStaffProfile(eq("rohan@test.com"), any(StaffProfileRequest.class),
                any(MultipartFile.class), any(), any());
    }

    @Test
    @DisplayName("updateStaffProfile: missing profile part returns 400")
    void updateStaffProfile_missingProfilePart_returns400() throws Exception {
        MockMultipartFile cert = new MockMultipartFile("stateBoardCertificate", "cert.pdf",
                MediaType.APPLICATION_PDF_VALUE, new byte[]{1});

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/auth/staff-profile")
                        .file(cert)
                        .principal(new UsernamePasswordAuthenticationToken("rohan@test.com", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required part: profile"));

        verify(authService, never()).updateStaffProfile(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateStaffProfile: invalid aadhar returns 400 with field error")
    void updateStaffProfile_invalidAadhar_returns400() throws Exception {
        StaffProfileRequest request = new StaffProfileRequest("123", null, null);
        MockMultipartFile profile = new MockMultipartFile("profile", "profile.json",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/auth/staff-profile")
                        .file(profile)
                        .principal(new UsernamePasswordAuthenticationToken("rohan@test.com", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.aadharCardNumber").value("Aadhar card number must be exactly 12 digits"));

        verify(authService, never()).updateStaffProfile(any(), any(), any(), any(), any());
    }

    // =====================================================================
    //  verifyStaffProfile (PATCH /api/auth/staff/{userId}/verification - admin)
    // =====================================================================

    @Test
    @DisplayName("verifyStaffProfile: admin verifies staff returns 200 with verified=true")
    void verifyStaffProfile_validRequest_returns200() throws Exception {
        StaffVerificationRequest request = new StaffVerificationRequest(true);

        StaffProfileResponseDto responseDto = StaffProfileResponseDto.builder()
                .id(10L)
                .staffCategory("ICU Nurse")
                .verified(true)
                .build();

        when(authService.verifyStaffProfile(5L, true)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/auth/staff/5/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        verify(authService).verifyStaffProfile(5L, true);
    }

    @Test
    @DisplayName("verifyStaffProfile: missing verified returns 400 with field error")
    void verifyStaffProfile_missingVerified_returns400() throws Exception {
        mockMvc.perform(patch("/api/auth/staff/5/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.verified").value("Verified status is required"));

        verify(authService, never()).verifyStaffProfile(anyLong(), anyBoolean());
    }

    // =====================================================================
    //  getStaffProfile (GET /api/auth/staff-profile - self-service)
    // =====================================================================

    @Test
    @DisplayName("getStaffProfile: returns the authenticated staff profile with verified flag")
    void getStaffProfile_validRequest_returns200() throws Exception {
        StaffProfileResponseDto responseDto = StaffProfileResponseDto.builder()
                .id(10L)
                .staffCategory("ICU Nurse")
                .verified(true)
                .build();

        when(authService.getStaffProfile("rohan@test.com")).thenReturn(responseDto);

        mockMvc.perform(get("/api/auth/staff-profile")
                        .principal(new UsernamePasswordAuthenticationToken("rohan@test.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        verify(authService).getStaffProfile("rohan@test.com");
    }

    @Test
    @DisplayName("getAllStaffProfiles: admin lists all staff profiles")
    void getAllStaffProfiles_returns200() throws Exception {
        StaffProfileResponseDto responseDto = StaffProfileResponseDto.builder()
                .id(10L)
                .firstName("Rohan")
                .lastName("Mehta")
                .email("rohan@test.com")
                .staffCategory("ICU Nurse")
                .verified(true)
                .build();

        Page<StaffProfileResponseDto> page = new PageImpl<>(
                java.util.List.of(responseDto), PageRequest.of(0, 10), 1);
        when(authService.getAllStaffProfiles(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/auth/staff").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Rohan"))
                .andExpect(jsonPath("$.content[0].email").value("rohan@test.com"))
                .andExpect(jsonPath("$.content[0].staffCategory").value("ICU Nurse"))
                .andExpect(jsonPath("$.content[0].verified").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0));

        verify(authService).getAllStaffProfiles(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("getAllStaffProfiles: defaults page to 0 and size to 10")
    void getAllStaffProfiles_defaultPaging_returns200() throws Exception {
        Page<StaffProfileResponseDto> page = new PageImpl<>(
                java.util.List.of(), PageRequest.of(0, 10), 0);
        when(authService.getAllStaffProfiles(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/auth/staff"))
                .andExpect(status().isOk());

        verify(authService).getAllStaffProfiles(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("getAllStaffProfiles: caps size at 100 and clamps negative values")
    void getAllStaffProfiles_clampsPagingParams_returns200() throws Exception {
        Page<StaffProfileResponseDto> page = new PageImpl<>(
                java.util.List.of(), PageRequest.of(0, 100), 0);
        when(authService.getAllStaffProfiles(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/auth/staff")
                        .param("page", "-3")
                        .param("size", "999"))
                .andExpect(status().isOk());

        verify(authService).getAllStaffProfiles(PageRequest.of(0, 100));
    }

    // =====================================================================
    //  updateClientProfile (PUT /api/auth/client-profile)
    // =====================================================================

    @Test
    @DisplayName("updateClientProfile: valid request returns 200 with updated user")
    void updateClientProfile_validRequest_returns200() throws Exception {
        ClientProfileRequest request = new ClientProfileRequest("Riya", "Sharma", "9876543210");

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .email("riya@test.com")
                .firstName("Riya")
                .lastName("Sharma")
                .phone("9876543210")
                .role(Role.ROLE_USER)
                .build();

        when(authService.updateClientProfile(eq("riya@test.com"), any(ClientProfileRequest.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/auth/client-profile")
                        .principal(new UsernamePasswordAuthenticationToken("riya@test.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Riya"))
                .andExpect(jsonPath("$.lastName").value("Sharma"))
                .andExpect(jsonPath("$.phone").value("9876543210"));

        verify(authService).updateClientProfile(eq("riya@test.com"), any(ClientProfileRequest.class));
    }

    @Test
    @DisplayName("updateClientProfile: invalid mobileNumber returns 400 with field error")
    void updateClientProfile_invalidMobileNumber_returns400() throws Exception {
        ClientProfileRequest request = new ClientProfileRequest("Riya", "Sharma", "abc");

        mockMvc.perform(put("/api/auth/client-profile")
                        .principal(new UsernamePasswordAuthenticationToken("riya@test.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mobileNumber").value(
                        "Mobile number must be 10-15 digits, optionally starting with +"));

        verify(authService, never()).updateClientProfile(any(), any());
    }

    @Test
    @DisplayName("updateClientProfile: user not found maps to 404")
    void updateClientProfile_userNotFound_returns404() throws Exception {
        ClientProfileRequest request = new ClientProfileRequest("Riya", "Sharma", "9876543210");

        when(authService.updateClientProfile(eq("riya@test.com"), any(ClientProfileRequest.class)))
                .thenThrow(new UserNotFoundException("User not found with email : riya@test.com"));

        mockMvc.perform(put("/api/auth/client-profile")
                        .principal(new UsernamePasswordAuthenticationToken("riya@test.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with email : riya@test.com"));
    }
}
