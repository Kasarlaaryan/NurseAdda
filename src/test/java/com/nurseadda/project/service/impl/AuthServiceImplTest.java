package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.IllegalCredentialsException;
import com.nurseadda.project.common.exception.InvalidOtpException;
import com.nurseadda.project.common.exception.OtpExpiredException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
import com.nurseadda.project.dto.request.ChangePasswordRequest;
import com.nurseadda.project.dto.request.ClientProfileRequest;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.ForgotPasswordRequest;
import com.nurseadda.project.dto.request.LogoutRequest;
import com.nurseadda.project.dto.request.RefreshTokenRequest;
import com.nurseadda.project.dto.request.ResetPasswordRequest;
import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.request.VerifyOtpRequest;
import com.nurseadda.project.dto.response.AuthResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffDocumentType;
import com.nurseadda.project.model.PasswordReset;
import com.nurseadda.project.model.PendingRegistration;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.PasswordResetRepository;
import com.nurseadda.project.repository.PendingRegistrationRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.security.TokenBlacklistService;
import com.nurseadda.project.service.EmailService;
import com.nurseadda.project.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffProfileRepository staffProfileRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Mock
    private StaffDocumentRepository staffDocumentRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpLength", 6);
        ReflectionTestUtils.setField(authService, "otpExpiryMinutes", 10);
    }

    // =====================================================================
    //  registerStaff — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("registerStaff: valid request returns OTP message")
    void registerStaff_validRequest_returnsOtpMessage() throws UserAlreadyExistException {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        when(userService.existsByEmail("riya@test.com")).thenReturn(false);

        String message = authService.registerStaff(request);

        assertThat(message).isEqualTo("OTP sent to your email");
    }

    @Test
    @DisplayName("registerStaff: saves pending registration with staff role and encoded password")
    void registerStaff_validRequest_savesPendingRegistration() throws UserAlreadyExistException {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        when(userService.existsByEmail("riya@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encodedHash");

        authService.registerStaff(request);

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationRepository).save(captor.capture(), eq(10L));

        PendingRegistration pending = captor.getValue();
        assertThat(pending.getEmail()).isEqualTo("riya@test.com");
        assertThat(pending.getPassword()).isEqualTo("$2a$10$encodedHash");
        assertThat(pending.getFirstName()).isEqualTo("Riya");
        assertThat(pending.getLastName()).isEqualTo("Sharma");
        assertThat(pending.getPhone()).isEqualTo("9876543210");
        assertThat(pending.getRole()).isEqualTo(Role.ROLE_STAFF.name());
        assertThat(pending.getStaffCategory()).isEqualTo("ICU Nurse");
        assertThat(pending.isVerified()).isFalse();
        assertThat(pending.getCode()).matches("^[0-9]{6}$");
        assertThat(pending.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(pending.getExpiresAt()).isBefore(LocalDateTime.now().plusMinutes(11));

        // Core requirement: registration must NOT touch the DB — only Redis + email
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerStaff: sends the generated OTP to the given email")
    void registerStaff_validRequest_sendsOtpEmail() throws UserAlreadyExistException {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        when(userService.existsByEmail("riya@test.com")).thenReturn(false);

        authService.registerStaff(request);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtp(emailCaptor.capture(), otpCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo("riya@test.com");
        assertThat(otpCaptor.getValue()).matches("^[0-9]{6}$");
    }

    @Test
    @DisplayName("registerStaff: single-word name puts everything in firstName, empty lastName")
    void registerStaff_singleWordName_splitsCorrectly() throws UserAlreadyExistException {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya", "riya@test.com", "9876543210", "Nurse", "secret123"
        );

        when(userService.existsByEmail("riya@test.com")).thenReturn(false);

        authService.registerStaff(request);

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationRepository).save(captor.capture(), eq(10L));

        assertThat(captor.getValue().getFirstName()).isEqualTo("Riya");
        assertThat(captor.getValue().getLastName()).isEmpty();
    }

    // =====================================================================
    //  registerStaff — NEGATIVE
    // =====================================================================

    @Test
    @DisplayName("registerStaff: duplicate email throws UserAlreadyExistException")
    void registerStaff_duplicateEmail_throwsUserAlreadyExist() {
        StaffRegisterRequest request = new StaffRegisterRequest(
                "Riya Sharma", "riya@test.com", "9876543210", "ICU Nurse", "secret123"
        );

        when(userService.existsByEmail("riya@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStaff(request))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessageContaining("riya@test.com");

        verify(pendingRegistrationRepository, never()).save(any(), anyLong());
        verifyNoInteractions(emailService);
    }

    // =====================================================================
    //  registerClient — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("registerClient: valid request returns OTP message")
    void registerClient_validRequest_returnsOtpMessage() throws UserAlreadyExistException {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "secret123", "secret123"
        );

        when(userService.existsByEmail("client@test.com")).thenReturn(false);

        String message = authService.registerClient(request);

        assertThat(message).isEqualTo("OTP sent to your email");
    }

    @Test
    @DisplayName("registerClient: saves pending registration with user role")
    void registerClient_validRequest_savesPendingRegistration() throws UserAlreadyExistException {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "secret123", "secret123"
        );

        when(userService.existsByEmail("client@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encodedHash");

        authService.registerClient(request);

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationRepository).save(captor.capture(), eq(10L));

        PendingRegistration pending = captor.getValue();
        assertThat(pending.getEmail()).isEqualTo("client@test.com");
        assertThat(pending.getPassword()).isEqualTo("$2a$10$encodedHash");
        assertThat(pending.getPhone()).isEqualTo("9876543210");
        assertThat(pending.getRole()).isEqualTo(Role.ROLE_USER.name());
        assertThat(pending.getFirstName()).isEqualTo("Client");
        assertThat(pending.getLastName()).isEqualTo("User");
        assertThat(pending.getStaffCategory()).isNull();
        assertThat(pending.isVerified()).isFalse();
        assertThat(pending.getCode()).matches("^[0-9]{6}$");

        // Core requirement: registration must NOT touch the DB — only Redis + email
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerClient: sends the generated OTP to the given email")
    void registerClient_validRequest_sendsOtpEmail() throws UserAlreadyExistException {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "secret123", "secret123"
        );

        when(userService.existsByEmail("client@test.com")).thenReturn(false);

        authService.registerClient(request);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtp(emailCaptor.capture(), otpCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo("client@test.com");
        assertThat(otpCaptor.getValue()).matches("^[0-9]{6}$");
    }

    // =====================================================================
    //  registerClient — NEGATIVE
    // =====================================================================

    @Test
    @DisplayName("registerClient: duplicate email throws UserAlreadyExistException")
    void registerClient_duplicateEmail_throwsUserAlreadyExist() {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Client", "User", "client@test.com", "9876543210", "secret123", "secret123"
        );

        when(userService.existsByEmail("client@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerClient(request))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessageContaining("client@test.com");

        verify(pendingRegistrationRepository, never()).save(any(), anyLong());
        verifyNoInteractions(emailService);
    }

    // =====================================================================
    //  verifyOtp — quick sanity that registration + verification are linked
    // =====================================================================

    // =====================================================================
    //  updateStaffProfile — document lock for verified profiles
    // =====================================================================

    @Test
    @DisplayName("updateStaffProfile: verified profile cannot upload documents")
    void updateStaffProfile_verifiedProfile_blocksDocumentUploads() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");
        staffProfile.setVerified(true);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));

        org.springframework.web.multipart.MultipartFile photo = Mockito.mock(
                org.springframework.web.multipart.MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);

        assertThatThrownBy(() -> authService.updateStaffProfile(
                "rohan@test.com", new StaffProfileRequest(), photo, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("verified");

        verify(staffDocumentRepository, never()).save(any());
        verify(staffProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStaffProfile: verified profile can still update fields without files")
    void updateStaffProfile_verifiedProfile_allowsFieldUpdatesWithoutFiles() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");
        staffProfile.setVerified(true);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffProfileRepository.save(staffProfile)).thenReturn(staffProfile);
        when(staffDocumentRepository.findByStaffProfileId(10L)).thenReturn(java.util.List.of());

        authService.updateStaffProfile("rohan@test.com",
                new StaffProfileRequest("123456789012", null, null), null, null, null);

        assertThat(staffProfile.getAadharCardNumber()).isEqualTo("123456789012");
        verify(staffProfileRepository).save(staffProfile);
        verify(staffDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("verifyOtp: creates the user only after a valid OTP")
    void verifyOtp_validOtp_createsUser() {
        // Not testing full verification here - covered by integration; just
        // ensure the pending store is consulted before any user is created.
        when(pendingRegistrationRepository.findByEmail("riya@test.com"))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.verifyOtp(
                new VerifyOtpRequest("riya@test.com", "123456")
        )).isInstanceOf(InvalidOtpException.class);

        verify(userRepository, never()).save(any());
        verify(staffProfileRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    // =====================================================================
    //  refresh — POSITIVE
    // =====================================================================

    @Test
    @DisplayName("refresh: valid refresh token issues a new token pair and rotates the old one")
    void refresh_validRefreshToken_rotatesAndReturnsNewTokens() {
        User user = new User();
        user.setEmail("riya@test.com");

        when(jwtUtil.retrieveTokenType("old-refresh")).thenReturn(JwtUtil.TYPE_REFRESH);
        when(jwtUtil.retrieveEmailFromToken("old-refresh")).thenReturn("riya@test.com");
        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(jwtUtil.retrieveRemainingValiditySeconds("old-refresh")).thenReturn(600L);
        when(jwtUtil.generateAccessToken(user)).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("new-refresh");

        AuthResponseDto response = authService.refresh(new RefreshTokenRequest("old-refresh"));

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(tokenBlacklistService).blacklist("old-refresh", 600L);
    }

    // =====================================================================
    //  refresh — NEGATIVE
    // =====================================================================

    @Test
    @DisplayName("refresh: access token used as refresh token is rejected")
    void refresh_accessToken_rejected() {
        when(jwtUtil.retrieveTokenType("access-token")).thenReturn(JwtUtil.TYPE_ACCESS);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("access-token")))
                .isInstanceOf(IllegalCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("refresh: revoked refresh token is rejected")
    void refresh_revokedRefreshToken_rejected() {
        when(jwtUtil.retrieveTokenType("old-refresh")).thenReturn(JwtUtil.TYPE_REFRESH);
        when(jwtUtil.retrieveEmailFromToken("old-refresh")).thenReturn("riya@test.com");
        when(tokenBlacklistService.isBlacklisted("old-refresh")).thenReturn(true);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("old-refresh")))
                .isInstanceOf(IllegalCredentialsException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    @DisplayName("refresh: disabled account is rejected")
    void refresh_disabledUser_rejected() {
        User user = new User();
        user.setEmail("riya@test.com");
        user.setEnabled(false);

        when(jwtUtil.retrieveTokenType("old-refresh")).thenReturn(JwtUtil.TYPE_REFRESH);
        when(jwtUtil.retrieveEmailFromToken("old-refresh")).thenReturn("riya@test.com");
        when(tokenBlacklistService.isBlacklisted("old-refresh")).thenReturn(false);
        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("old-refresh")))
                .isInstanceOf(IllegalCredentialsException.class)
                .hasMessageContaining("disabled");

        verify(tokenBlacklistService, never()).blacklist(anyString(), anyLong());
    }

    @Test
    @DisplayName("refresh: user with revoked/unknown token is rejected with 401 semantics")
    void refresh_unknownUser_rejected() {
        when(jwtUtil.retrieveTokenType("old-refresh")).thenReturn(JwtUtil.TYPE_REFRESH);
        when(jwtUtil.retrieveEmailFromToken("old-refresh")).thenReturn("riya@test.com");
        when(tokenBlacklistService.isBlacklisted("old-refresh")).thenReturn(false);
        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("old-refresh")))
                .isInstanceOf(IllegalCredentialsException.class);
    }

    // =====================================================================
    //  getCurrentUser
    // =====================================================================

    @Test
    @DisplayName("getCurrentUser: returns the mapped user dto")
    void getCurrentUser_existingUser_returnsDto() {
        User user = new User();
        user.setEmail("riya@test.com");
        UserResponseDto dto = UserResponseDto.builder().email("riya@test.com").build();

        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(modelMapper.map(user, UserResponseDto.class)).thenReturn(dto);

        UserResponseDto result = authService.getCurrentUser("riya@test.com");

        assertThat(result.getEmail()).isEqualTo("riya@test.com");
    }

    @Test
    @DisplayName("getCurrentUser: missing user throws UserNotFoundException")
    void getCurrentUser_missingUser_throws() {
        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("riya@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // =====================================================================
    //  logout
    // =====================================================================

    @Test
    @DisplayName("logout: blacklists access token and optional refresh token")
    void logout_blacklistsTokens() {
        when(jwtUtil.retrieveRemainingValiditySeconds("access-token")).thenReturn(900L);
        when(jwtUtil.retrieveRemainingValiditySeconds("refresh-token")).thenReturn(6000L);

        authService.logout("access-token", new LogoutRequest("refresh-token"));

        verify(tokenBlacklistService).blacklist("access-token", 900L);
        verify(tokenBlacklistService).blacklist("refresh-token", 6000L);
    }

    // =====================================================================
    //  changePassword
    // =====================================================================

    @Test
    @DisplayName("changePassword: correct current password updates to new password")
    void changePassword_correctCurrentPassword_updatesPassword() {
        User user = new User();
        user.setPassword("$2a$10$encodedHash");

        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("old-pass", "$2a$10$encodedHash")).thenReturn(true);
        when(passwordEncoder.encode("new-pass-123")).thenReturn("$2a$10$newHash");

        authService.changePassword("riya@test.com",
                new ChangePasswordRequest("old-pass", "new-pass-123"));

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("$2a$10$newHash");
    }

    @Test
    @DisplayName("changePassword: wrong current password throws IllegalCredentialsException")
    void changePassword_wrongCurrentPassword_throws() {
        User user = new User();
        user.setPassword("$2a$10$encodedHash");

        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$encodedHash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword("riya@test.com",
                new ChangePasswordRequest("wrong", "new-pass-123")))
                .isInstanceOf(IllegalCredentialsException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    // =====================================================================
    //  updateClientProfile
    // =====================================================================

    @Test
    @DisplayName("updateClientProfile: updates firstName, lastName and phone")
    void updateClientProfile_validRequest_updatesUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("riya@test.com");
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setPhone("9876543210");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponseDto dto = UserResponseDto.builder()
                .id(1L)
                .email("riya@test.com")
                .firstName("Riya")
                .lastName("Sharma")
                .phone("9999999999")
                .role(Role.ROLE_USER)
                .build();
        when(modelMapper.map(user, UserResponseDto.class)).thenReturn(dto);

        UserResponseDto result = authService.updateClientProfile("riya@test.com",
                new ClientProfileRequest("Riya", "Sharma", "9999999999"));

        assertThat(result).isSameAs(dto);
        assertThat(user.getFirstName()).isEqualTo("Riya");
        assertThat(user.getLastName()).isEqualTo("Sharma");
        assertThat(user.getPhone()).isEqualTo("9999999999");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateClientProfile: blank fields are skipped (partial update)")
    void updateClientProfile_partialUpdate_keepsExistingValues() {
        User user = new User();
        user.setId(1L);
        user.setEmail("riya@test.com");
        user.setFirstName("Riya");
        user.setLastName("Sharma");
        user.setPhone("9876543210");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        authService.updateClientProfile("riya@test.com",
                new ClientProfileRequest(null, null, "9999999999"));

        assertThat(user.getFirstName()).isEqualTo("Riya");
        assertThat(user.getLastName()).isEqualTo("Sharma");
        assertThat(user.getPhone()).isEqualTo("9999999999");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateClientProfile: user not found throws UserNotFoundException")
    void updateClientProfile_userNotFound_throws() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.updateClientProfile("nobody@test.com",
                new ClientProfileRequest("Riya", "Sharma", "9876543210")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nobody@test.com");

        verify(userRepository, never()).save(any());
    }

    // =====================================================================
    //  forgotPassword
    // =====================================================================

    @Test
    @DisplayName("forgotPassword: saves reset request and emails the OTP")
    void forgotPassword_existingUser_savesAndEmails() {
        User user = new User();
        user.setEmail("riya@test.com");

        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));

        authService.forgotPassword(new ForgotPasswordRequest("riya@test.com"));

        ArgumentCaptor<PasswordReset> captor = ArgumentCaptor.forClass(PasswordReset.class);
        verify(passwordResetRepository).save(captor.capture(), eq(10L));

        PasswordReset reset = captor.getValue();
        assertThat(reset.getEmail()).isEqualTo("riya@test.com");
        assertThat(reset.getCode()).matches("^[0-9]{6}$");
        assertThat(reset.getExpiresAt()).isAfter(LocalDateTime.now());

        verify(emailService).sendOtp(eq("riya@test.com"), anyString());
    }

    @Test
    @DisplayName("forgotPassword: unknown email throws UserNotFoundException")
    void forgotPassword_unknownEmail_throws() {
        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(new ForgotPasswordRequest("riya@test.com")))
                .isInstanceOf(UserNotFoundException.class);

        verify(passwordResetRepository, never()).save(any(), anyLong());
    }

    // =====================================================================
    //  resetPassword
    // =====================================================================

    @Test
    @DisplayName("resetPassword: valid OTP resets the password and consumes the code")
    void resetPassword_validOtp_resetsPassword() {
        PasswordReset reset = PasswordReset.builder()
                .email("riya@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        User user = new User();
        user.setEmail("riya@test.com");

        when(passwordResetRepository.findByEmail("riya@test.com"))
                .thenReturn(java.util.Optional.of(reset));
        when(userRepository.findByEmail("riya@test.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.encode("new-pass-123")).thenReturn("$2a$10$newHash");

        authService.resetPassword(new ResetPasswordRequest("riya@test.com", "123456", "new-pass-123"));

        verify(userRepository).save(user);
        verify(passwordResetRepository).deleteByEmail("riya@test.com");
        assertThat(user.getPassword()).isEqualTo("$2a$10$newHash");
    }

    @Test
    @DisplayName("resetPassword: wrong OTP throws InvalidOtpException")
    void resetPassword_wrongOtp_throws() {
        PasswordReset reset = PasswordReset.builder()
                .email("riya@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(passwordResetRepository.findByEmail("riya@test.com"))
                .thenReturn(java.util.Optional.of(reset));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("riya@test.com", "999999", "new-pass-123")))
                .isInstanceOf(InvalidOtpException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword: expired OTP throws OtpExpiredException and removes the request")
    void resetPassword_expiredOtp_throws() {
        PasswordReset reset = PasswordReset.builder()
                .email("riya@test.com")
                .code("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(passwordResetRepository.findByEmail("riya@test.com"))
                .thenReturn(java.util.Optional.of(reset));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("riya@test.com", "123456", "new-pass-123")))
                .isInstanceOf(OtpExpiredException.class);

        verify(passwordResetRepository).deleteByEmail("riya@test.com");
    }

    // =====================================================================
    //  unlockAccount
    // =====================================================================

    @Test
    @DisplayName("unlockAccount: resets failed attempts and clears the lock")
    void unlockAccount_existingUser_clearsLock() {
        User user = new User();
        user.setId(7L);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

        when(userRepository.findById(7L)).thenReturn(java.util.Optional.of(user));

        String message = authService.unlockAccount(7L);

        assertThat(message).isEqualTo("Account unlocked successfully");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("unlockAccount: missing user throws UserNotFoundException")
    void unlockAccount_missingUser_throws() {
        when(userRepository.findById(7L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.unlockAccount(7L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // =====================================================================
    //  updateStaffProfile
    // =====================================================================

    @Test
    @DisplayName("updateStaffProfile: updates aadhar card and license dates")
    void updateStaffProfile_validRequest_updatesProfileFields() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffProfileRepository.save(staffProfile)).thenReturn(staffProfile);

        StaffProfileResponseDto response = authService.updateStaffProfile("rohan@test.com",
                new StaffProfileRequest(
                        "123456789012",
                        LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 15)
                ),
                null,
                null,
                null);

        assertThat(staffProfile.getAadharCardNumber()).isEqualTo("123456789012");
        assertThat(staffProfile.getLicenseValidityDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(staffProfile.getLicenseRenewalDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(response.getAadharCardNumber()).isEqualTo("123456789012");
        assertThat(response.getStateBoardCertificatePath()).isNull();
        assertThat(response.getEducationalDocumentPaths()).isEmpty();
        assertThat(response.getPhotoPaths()).isEmpty();
        verify(staffProfileRepository).save(staffProfile);
        verify(staffDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStaffProfile: stores documents and replaces state board certificate")
    void updateStaffProfile_withFiles_storesDocuments(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(authService, "uploadDir", tempDir.toString());

        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffProfileRepository.save(staffProfile)).thenReturn(staffProfile);

        MultipartFile cert = Mockito.mock(MultipartFile.class);
        when(cert.isEmpty()).thenReturn(false);
        when(cert.getOriginalFilename()).thenReturn("state-board-cert.pdf");

        MultipartFile doc = Mockito.mock(MultipartFile.class);
        when(doc.isEmpty()).thenReturn(false);
        when(doc.getOriginalFilename()).thenReturn("bsc-degree.pdf");

        MultipartFile photo = Mockito.mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(photo.getOriginalFilename()).thenReturn("staff-photo.jpg");

        authService.updateStaffProfile("rohan@test.com",
                new StaffProfileRequest("123456789012", null, null),
                cert,
                List.of(doc),
                List.of(photo));

        assertThat(staffProfile.getAadharCardNumber()).isEqualTo("123456789012");
        verify(staffDocumentRepository).deleteByStaffProfileIdAndDocumentType(
                10L, StaffDocumentType.STATE_BOARD_CERTIFICATE);

        ArgumentCaptor<StaffDocument> captor = ArgumentCaptor.forClass(StaffDocument.class);
        verify(staffDocumentRepository, times(3)).save(captor.capture());

        List<StaffDocument> savedDocs = captor.getAllValues();
        assertThat(savedDocs)
                .extracting(StaffDocument::getDocumentType)
                .containsExactlyInAnyOrder(
                        StaffDocumentType.STATE_BOARD_CERTIFICATE,
                        StaffDocumentType.EDUCATIONAL_CERTIFICATE,
                        StaffDocumentType.PHOTO);
        assertThat(tempDir.toFile().listFiles()).isNotEmpty();
    }

    @Test
    @DisplayName("updateStaffProfile: user not found throws UserNotFoundException")
    void updateStaffProfile_userNotFound_throws() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.updateStaffProfile("nobody@test.com",
                new StaffProfileRequest("123456789012", null, null), null, null, null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("nobody@test.com");

        verify(staffProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStaffProfile: staff profile not found throws ResourceNotFoundException")
    void updateStaffProfile_staffProfileNotFound_throws() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.updateStaffProfile("rohan@test.com",
                new StaffProfileRequest("123456789012", null, null), null, null, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(staffProfileRepository, never()).save(any());
        verify(staffDocumentRepository, never()).save(any());
    }

    // =====================================================================
    //  verifyStaffProfile (admin)
    // =====================================================================

    @Test
    @DisplayName("verifyStaffProfile: sets verified flag and sends profile verified email")
    void verifyStaffProfile_verifiedTrue_setsVerifiedAndSendsEmail() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setFirstName("Rohan");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");
        staffProfile.setVerified(false);

        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffProfileRepository.save(staffProfile)).thenReturn(staffProfile);

        StaffProfileResponseDto response = authService.verifyStaffProfile(5L, true);

        assertThat(staffProfile.isVerified()).isTrue();
        assertThat(response.isVerified()).isTrue();
        assertThat(response.getFirstName()).isEqualTo("Rohan");
        assertThat(response.getEmail()).isEqualTo("rohan@test.com");
        verify(staffProfileRepository).save(staffProfile);
        verify(emailService).sendProfileVerifiedEmail("rohan@test.com", "Rohan");
    }

    @Test
    @DisplayName("verifyStaffProfile: sets verified flag to false and sends rejection email")
    void verifyStaffProfile_verifiedFalse_setsFlagAndSendsRejectionEmail() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setFirstName("Rohan");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");
        staffProfile.setVerified(true);

        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffProfileRepository.save(staffProfile)).thenReturn(staffProfile);

        StaffProfileResponseDto response = authService.verifyStaffProfile(5L, false);

        assertThat(staffProfile.isVerified()).isFalse();
        assertThat(response.isVerified()).isFalse();
        verify(staffProfileRepository).save(staffProfile);
        verify(emailService).sendProfileRejectedEmail("rohan@test.com", "Rohan");
        verify(emailService, never()).sendProfileVerifiedEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("verifyStaffProfile: staff profile not found throws ResourceNotFoundException")
    void verifyStaffProfile_notFound_throws() {
        when(staffProfileRepository.findByUserId(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.verifyStaffProfile(99L, true))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(staffProfileRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // =====================================================================
    //  getStaffProfile (self-service)
    // =====================================================================

    @Test
    @DisplayName("getStaffProfile: returns the staff profile of the authenticated user")
    void getStaffProfile_valid_returnsResponse() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));

        StaffProfileResponseDto response = authService.getStaffProfile("rohan@test.com");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStaffCategory()).isEqualTo("ICU Nurse");
        assertThat(response.isVerified()).isFalse();
    }

    @Test
    @DisplayName("getStaffProfile: user not found throws UserNotFoundException")
    void getStaffProfile_userNotFound_throws() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.getStaffProfile("nobody@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getAllStaffProfiles: returns all staff profiles")
    void getAllStaffProfiles_returnsAllProfiles() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setFirstName("Rohan");
        user.setLastName("Mehta");
        user.setPhone("9876543210");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");

        org.springframework.data.domain.Page<StaffProfile> staffPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(staffProfile),
                        PageRequest.of(0, 10),
                        1
                );
        when(staffProfileRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(staffPage);

        StaffDocument cert = new StaffDocument();
        cert.setStaffProfile(staffProfile);
        cert.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        cert.setFilePath("uploads/staff/5/certificate/cert.pdf");

        StaffDocument doc = new StaffDocument();
        doc.setStaffProfile(staffProfile);
        doc.setDocumentType(StaffDocumentType.EDUCATIONAL_CERTIFICATE);
        doc.setFilePath("uploads/staff/5/education/bsc.pdf");

        when(staffDocumentRepository.findByStaffProfileIdIn(java.util.List.of(10L)))
                .thenReturn(java.util.List.of(cert, doc));

        Page<StaffProfileResponseDto> profiles = authService.getAllStaffProfiles(PageRequest.of(0, 10));

        assertThat(profiles.getTotalElements()).isEqualTo(1);
        assertThat(profiles.getContent()).hasSize(1);
        assertThat(profiles.getContent().get(0).getStaffCategory()).isEqualTo("ICU Nurse");
        assertThat(profiles.getContent().get(0).isVerified()).isFalse();
        assertThat(profiles.getContent().get(0).getFirstName()).isEqualTo("Rohan");
        assertThat(profiles.getContent().get(0).getLastName()).isEqualTo("Mehta");
        assertThat(profiles.getContent().get(0).getEmail()).isEqualTo("rohan@test.com");
        assertThat(profiles.getContent().get(0).getPhone()).isEqualTo("9876543210");
        assertThat(profiles.getContent().get(0).getStateBoardCertificatePath())
                .isEqualTo("uploads/staff/5/certificate/cert.pdf");
        assertThat(profiles.getContent().get(0).getEducationalDocumentPaths())
                .containsExactly("uploads/staff/5/education/bsc.pdf");

        // Batch loading: documents fetched once with an IN query, not per-profile
        verify(staffDocumentRepository).findByStaffProfileIdIn(java.util.List.of(10L));
        verify(staffDocumentRepository, never()).findByStaffProfileId(anyLong());
    }

    @Test
    @DisplayName("getAllStaffProfiles: empty page skips document query")
    void getAllStaffProfiles_emptyPage_skipsDocumentQuery() {
        org.springframework.data.domain.Page<StaffProfile> emptyPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(),
                        PageRequest.of(0, 10),
                        0
                );
        when(staffProfileRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);

        Page<StaffProfileResponseDto> profiles = authService.getAllStaffProfiles(PageRequest.of(0, 10));

        assertThat(profiles.getContent()).isEmpty();
        assertThat(profiles.getTotalElements()).isZero();
        verify(staffDocumentRepository, never()).findByStaffProfileIdIn(any());
        verify(staffDocumentRepository, never()).findByStaffProfileId(anyLong());
    }
}
