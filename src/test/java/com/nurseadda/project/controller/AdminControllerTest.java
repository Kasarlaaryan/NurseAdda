package com.nurseadda.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nurseadda.project.common.exception.GlobalExceptionHandler;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
import com.nurseadda.project.dto.request.AdminCreateUserRequest;
import com.nurseadda.project.dto.request.AdminUpdateStaffProfileRequest;
import com.nurseadda.project.dto.request.AdminUpdateUserRequest;
import com.nurseadda.project.dto.request.DocumentReplacementRequest;
import com.nurseadda.project.dto.request.StaffDocumentVerificationRequest;
import com.nurseadda.project.dto.request.StaffVerificationRequest;
import com.nurseadda.project.dto.request.UserStatusRequest;
import com.nurseadda.project.dto.response.StaffDocumentResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffDocumentType;
import com.nurseadda.project.service.AdminService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    private final UsernamePasswordAuthenticationToken rootPrincipal =
            new UsernamePasswordAuthenticationToken("root@nurseadda.com", null);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =====================================================================
    //  GET /api/admin/users
    // =====================================================================

    @Test
    @DisplayName("getAllUsers: returns paginated users")
    void getAllUsers_returns200() throws Exception {
        UserResponseDto dto = UserResponseDto.builder()
                .id(2L).email("rohan@test.com").role(Role.ROLE_STAFF).enabled(true).build();

        Page<UserResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(adminService.getAllUsers(PageRequest.of(0, 10), null)).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("rohan@test.com"))
                .andExpect(jsonPath("$.content[0].role").value("ROLE_STAFF"))
                .andExpect(jsonPath("$.content[0].enabled").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(adminService).getAllUsers(PageRequest.of(0, 10), null);
    }

    @Test
    @DisplayName("getAllUsers: passes role filter and clamps paging params")
    void getAllUsers_roleAndClamping() throws Exception {
        Page<UserResponseDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(adminService.getAllUsers(PageRequest.of(0, 100), Role.ROLE_USER)).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "-3").param("size", "999").param("role", "ROLE_USER"))
                .andExpect(status().isOk());

        verify(adminService).getAllUsers(PageRequest.of(0, 100), Role.ROLE_USER);
    }

    @Test
    @DisplayName("getAllUsers: invalid role value returns 400")
    void getAllUsers_invalidRole_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/users").param("role", "NOT_A_ROLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter: role"));

        verify(adminService, never()).getAllUsers(any(), any());
    }

    // =====================================================================
    //  GET /api/admin/users/{id}
    // =====================================================================

    @Test
    @DisplayName("getUserById: returns the user")
    void getUserById_returns200() throws Exception {
        UserResponseDto dto = UserResponseDto.builder()
                .id(2L).email("rohan@test.com").role(Role.ROLE_STAFF).build();
        when(adminService.getUserById(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(adminService).getUserById(2L);
    }

    @Test
    @DisplayName("getUserById: not found maps to 404")
    void getUserById_notFound_returns404() throws Exception {
        when(adminService.getUserById(99L))
                .thenThrow(new UserNotFoundException("User not found with id : 99"));

        mockMvc.perform(get("/api/admin/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id : 99"));
    }

    // =====================================================================
    //  POST /api/admin/users
    // =====================================================================

    @Test
    @DisplayName("createUser: valid request returns 201")
    void createUser_validRequest_returns201() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Rohan", "Mehta", "rohan@test.com", "9876543210",
                "secret123", Role.ROLE_STAFF, "ICU Nurse"
        );

        UserResponseDto dto = UserResponseDto.builder()
                .id(2L).email("rohan@test.com").role(Role.ROLE_STAFF).enabled(true).build();
        when(adminService.createUser(any(AdminCreateUserRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("rohan@test.com"));

        verify(adminService).createUser(any(AdminCreateUserRequest.class));
    }

    @Test
    @DisplayName("createUser: duplicate email maps to 409")
    void createUser_duplicateEmail_returns409() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Rohan", "Mehta", "rohan@test.com", "9876543210",
                "secret123", Role.ROLE_STAFF, "ICU Nurse"
        );
        when(adminService.createUser(any(AdminCreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistException("User already exists with email : rohan@test.com"));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("createUser: blank password returns 400 with field error")
    void createUser_blankPassword_returns400() throws Exception {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Rohan", "Mehta", "rohan@test.com", "9876543210",
                "", Role.ROLE_STAFF, "ICU Nurse"
        );

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                // A blank password fails both @NotBlank and @Size; the constraint
                // evaluation order is not guaranteed, so accept either message.
                .andExpect(jsonPath("$.password").value(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is("Password is required"),
                        org.hamcrest.Matchers.is("Password must be between 6 and 72 characters")
                )));

        verify(adminService, never()).createUser(any());
    }

    @Test
    @DisplayName("createUser: missing role returns 400 with field error")
    void createUser_missingRole_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"R\",\"lastName\":\"M\",\"email\":\"r@t.com\",\"phone\":\"9876543210\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.role").value("Role is required"));

        verify(adminService, never()).createUser(any());
    }

    // =====================================================================
    //  PUT /api/admin/users/{id}
    // =====================================================================

    @Test
    @DisplayName("updateUser: valid request returns 200")
    void updateUser_validRequest_returns200() throws Exception {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                "Rohan", "Mehta", null, "9999999999", null, null, null
        );

        UserResponseDto dto = UserResponseDto.builder()
                .id(2L).email("rohan@test.com").role(Role.ROLE_STAFF).build();
        when(adminService.updateUser(eq(2L), any(AdminUpdateUserRequest.class),
                eq("root@nurseadda.com"))).thenReturn(dto);

        mockMvc.perform(put("/api/admin/users/2")
                        .principal(rootPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(adminService).updateUser(eq(2L), any(AdminUpdateUserRequest.class),
                eq("root@nurseadda.com"));
    }

    @Test
    @DisplayName("updateUser: invalid email returns 400")
    void updateUser_invalidEmail_returns400() throws Exception {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                null, null, "not-an-email", null, null, null, null
        );

        mockMvc.perform(put("/api/admin/users/2")
                        .principal(rootPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));

        verify(adminService, never()).updateUser(anyLong(), any(), any());
    }

    // =====================================================================
    //  DELETE /api/admin/users/{id}
    // =====================================================================

    @Test
    @DisplayName("deleteUser: valid request returns 200 with message")
    void deleteUser_validRequest_returns200() throws Exception {
        mockMvc.perform(delete("/api/admin/users/2").principal(rootPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(adminService).deleteUser(2L, "root@nurseadda.com");
    }

    @Test
    @DisplayName("deleteUser: self-delete guard maps to 400")
    void deleteUser_selfDelete_returns400() throws Exception {
        doThrow(new IllegalArgumentException("You cannot delete your own account"))
                .when(adminService).deleteUser(1L, "root@nurseadda.com");

        mockMvc.perform(delete("/api/admin/users/1").principal(rootPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot delete your own account"));
    }

    // =====================================================================
    //  PATCH /api/admin/users/{id}/status
    // =====================================================================

    @Test
    @DisplayName("updateUserStatus: disables the user")
    void updateUserStatus_disable_returns200() throws Exception {
        UserStatusRequest request = new UserStatusRequest(false);

        UserResponseDto dto = UserResponseDto.builder()
                .id(2L).email("rohan@test.com").role(Role.ROLE_STAFF).enabled(false).build();
        when(adminService.setUserEnabled(2L, false, "root@nurseadda.com")).thenReturn(dto);

        mockMvc.perform(patch("/api/admin/users/2/status")
                        .principal(rootPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        verify(adminService).setUserEnabled(2L, false, "root@nurseadda.com");
    }

    @Test
    @DisplayName("updateUserStatus: missing enabled returns 400")
    void updateUserStatus_missingEnabled_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/users/2/status")
                        .principal(rootPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.enabled").value("Enabled status is required"));

        verify(adminService, never()).setUserEnabled(anyLong(), anyBoolean(), any());
    }

    // =====================================================================
    //  Staff
    // =====================================================================

    @Test
    @DisplayName("getAllStaffProfiles: returns paginated staff")
    void getAllStaffProfiles_returns200() throws Exception {
        StaffProfileResponseDto dto = StaffProfileResponseDto.builder()
                .id(10L).email("rohan@test.com").staffCategory("ICU Nurse").build();
        Page<StaffProfileResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(adminService.getAllStaffProfiles(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/admin/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("rohan@test.com"));

        verify(adminService).getAllStaffProfiles(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("getStaffProfile: returns profile by user id")
    void getStaffProfile_returns200() throws Exception {
        StaffProfileResponseDto dto = StaffProfileResponseDto.builder()
                .id(10L).email("rohan@test.com").staffCategory("ICU Nurse").verified(true).build();
        when(adminService.getStaffProfileByUserId(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/staff/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        verify(adminService).getStaffProfileByUserId(5L);
    }

    @Test
    @DisplayName("updateStaffProfile: valid request returns 200")
    void updateStaffProfile_returns200() throws Exception {
        AdminUpdateStaffProfileRequest request = new AdminUpdateStaffProfileRequest(
                "Ward Nurse", "123456789012", LocalDate.of(2026, 12, 31), null
        );

        StaffProfileResponseDto dto = StaffProfileResponseDto.builder()
                .id(10L).staffCategory("Ward Nurse").build();
        when(adminService.updateStaffProfileByAdmin(5L, request)).thenReturn(dto);

        mockMvc.perform(put("/api/admin/staff/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffCategory").value("Ward Nurse"));

        verify(adminService).updateStaffProfileByAdmin(5L, request);
    }

    @Test
    @DisplayName("updateStaffProfile: invalid aadhar returns 400")
    void updateStaffProfile_invalidAadhar_returns400() throws Exception {
        AdminUpdateStaffProfileRequest request = new AdminUpdateStaffProfileRequest(
                null, "123", null, null
        );

        mockMvc.perform(put("/api/admin/staff/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.aadharCardNumber").value("Aadhar card number must be exactly 12 digits"));

        verify(adminService, never()).updateStaffProfileByAdmin(anyLong(), any());
    }

    @Test
    @DisplayName("verifyStaffProfile: verifies or rejects the profile")
    void verifyStaffProfile_returns200() throws Exception {
        StaffVerificationRequest request = new StaffVerificationRequest(true);

        StaffProfileResponseDto dto = StaffProfileResponseDto.builder()
                .id(10L).verified(true).build();
        when(adminService.verifyStaffProfile(5L, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/admin/staff/5/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        verify(adminService).verifyStaffProfile(5L, true);
    }

    // =====================================================================
    //  Documents
    // =====================================================================

    @Test
    @DisplayName("getStaffDocuments: returns document list")
    void getStaffDocuments_returns200() throws Exception {
        StaffDocumentResponseDto dto = StaffDocumentResponseDto.builder()
                .id(100L).documentType(StaffDocumentType.STATE_BOARD_CERTIFICATE)
                .fileName("cert.pdf").verified(true).build();
        when(adminService.getStaffDocuments(5L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/staff/5/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].documentType").value("STATE_BOARD_CERTIFICATE"))
                .andExpect(jsonPath("$[0].verified").value(true));

        verify(adminService).getStaffDocuments(5L);
    }

    @Test
    @DisplayName("verifyStaffDocument: verifies an individual document")
    void verifyStaffDocument_returns200() throws Exception {
        StaffDocumentVerificationRequest request = new StaffDocumentVerificationRequest(true);

        StaffDocumentResponseDto dto = StaffDocumentResponseDto.builder()
                .id(100L).verified(true).build();
        when(adminService.verifyStaffDocument(5L, 100L, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/admin/staff/5/documents/100/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        verify(adminService).verifyStaffDocument(5L, 100L, true);
    }

    @Test
    @DisplayName("verifyStaffDocument: missing verified returns 400")
    void verifyStaffDocument_missingVerified_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/staff/5/documents/100/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.verified").value("Verified status is required"));

        verify(adminService, never()).verifyStaffDocument(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("verifyStaffDocument: document not found maps to 404")
    void verifyStaffDocument_notFound_returns404() throws Exception {
        StaffDocumentVerificationRequest request = new StaffDocumentVerificationRequest(true);
        when(adminService.verifyStaffDocument(5L, 999L, true))
                .thenThrow(new ResourceNotFoundException("Document not found with id : 999 for user id : 5"));

        mockMvc.perform(patch("/api/admin/staff/5/documents/999/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Document not found with id : 999 for user id : 5"));
    }

    @Test
    @DisplayName("requestDocumentReplacement: requesting replacement returns 200")
    void requestDocumentReplacement_returns200() throws Exception {
        DocumentReplacementRequest request = new DocumentReplacementRequest(
                true, "Certificate expired", LocalDate.of(2025, 6, 30)
        );

        StaffDocumentResponseDto dto = StaffDocumentResponseDto.builder()
                .id(100L)
                .documentType(StaffDocumentType.STATE_BOARD_CERTIFICATE)
                .replacementRequested(true)
                .requestReason("Certificate expired")
                .expiryDate(LocalDate.of(2025, 6, 30))
                .build();
        when(adminService.requestDocumentReplacement(
                5L, 100L, true, "Certificate expired", LocalDate.of(2025, 6, 30)))
                .thenReturn(dto);

        mockMvc.perform(patch("/api/admin/staff/5/documents/100/replacement-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replacementRequested").value(true))
                .andExpect(jsonPath("$.requestReason").value("Certificate expired"));

        verify(adminService).requestDocumentReplacement(
                5L, 100L, true, "Certificate expired", LocalDate.of(2025, 6, 30));
    }

    @Test
    @DisplayName("requestDocumentReplacement: missing requested returns 400")
    void requestDocumentReplacement_missingRequested_returns400() throws Exception {
        mockMvc.perform(patch("/api/admin/staff/5/documents/100/replacement-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requested").value("Requested status is required"));

        verify(adminService, never()).requestDocumentReplacement(anyLong(), anyLong(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("requestDocumentReplacement: document not found maps to 404")
    void requestDocumentReplacement_notFound_returns404() throws Exception {
        DocumentReplacementRequest request = new DocumentReplacementRequest(true, "expired", null);
        when(adminService.requestDocumentReplacement(5L, 999L, true, "expired", null))
                .thenThrow(new ResourceNotFoundException("Document not found with id : 999 for user id : 5"));

        mockMvc.perform(patch("/api/admin/staff/5/documents/999/replacement-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Document not found with id : 999 for user id : 5"));
    }
}
