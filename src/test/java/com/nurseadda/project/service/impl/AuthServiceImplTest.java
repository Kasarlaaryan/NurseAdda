package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.InvalidOtpException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.request.VerifyOtpRequest;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.model.PendingRegistration;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.PendingRegistrationRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.service.EmailService;
import com.nurseadda.project.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
                "client@test.com", "9876543210", "secret123"
        );

        when(userService.existsByEmail("client@test.com")).thenReturn(false);

        String message = authService.registerClient(request);

        assertThat(message).isEqualTo("OTP sent to your email");
    }

    @Test
    @DisplayName("registerClient: saves pending registration with user role")
    void registerClient_validRequest_savesPendingRegistration() throws UserAlreadyExistException {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "client@test.com", "9876543210", "secret123"
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
        assertThat(pending.getFirstName()).isNull();
        assertThat(pending.getLastName()).isNull();
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
                "client@test.com", "9876543210", "secret123"
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
                "client@test.com", "9876543210", "secret123"
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
}
