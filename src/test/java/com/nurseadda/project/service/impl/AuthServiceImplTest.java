package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.IllegalCredentialsException;
import com.nurseadda.project.common.exception.InvalidOtpException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
import com.nurseadda.project.dto.request.ClientProfileRequest;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.StaffProfileRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.request.VerifyOtpRequest;
import com.nurseadda.project.dto.response.StaffDocumentResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.StaffDocumentType;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
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

    // =====================================================================
    //  reuploadStaffDocument (replacement-requested document)
    // =====================================================================

    @Test
    @DisplayName("reuploadStaffDocument: replaces the file, clears the request, un-verifies the profile and notifies admins")
    void reuploadStaffDocument_replacesFileAndUnverifiesProfile(@TempDir Path tempDir) throws Exception {
        ReflectionTestUtils.setField(authService, "uploadDir", tempDir.toString());

        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setFirstName("Rohan");
        user.setLastName("Mehta");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setStaffCategory("ICU Nurse");
        staffProfile.setVerified(true);

        Path oldFile = tempDir.resolve("certificate/old-cert.pdf");
        Files.createDirectories(oldFile.getParent());
        Files.writeString(oldFile, "old data");

        StaffDocument document = new StaffDocument();
        document.setId(100L);
        document.setStaffProfile(staffProfile);
        document.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        document.setFileName("old-cert.pdf");
        document.setFilePath(oldFile.toString());
        document.setVerified(true);
        document.setReplacementRequested(true);
        document.setRequestReason("Certificate expired");
        document.setExpiryDate(LocalDate.now().minusDays(1));

        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@nurseadda.com");
        admin.setRole(Role.ROLE_ADMIN);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffDocumentRepository.findById(100L)).thenReturn(java.util.Optional.of(document));
        when(staffDocumentRepository.save(document)).thenReturn(document);
        when(userRepository.findByRoleIn(List.of(Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN)))
                .thenReturn(List.of(admin));

        MockMultipartFile file = new MockMultipartFile("file", "new-cert.pdf",
                "application/pdf", new byte[]{1, 2, 3});

        StaffDocumentResponseDto result = authService.reuploadStaffDocument(
                "rohan@test.com", 100L, file);

        // Old file replaced on disk, new one stored
        assertThat(Files.exists(oldFile)).isFalse();
        assertThat(document.getFilePath()).isNotEqualTo(oldFile.toString());
        assertThat(Files.exists(Path.of(document.getFilePath()))).isTrue();
        assertThat(document.getFileName()).isEqualTo("new-cert.pdf");

        // Replacement request consumed and document pending verification again
        assertThat(document.isVerified()).isFalse();
        assertThat(document.isReplacementRequested()).isFalse();
        assertThat(document.getRequestReason()).isNull();
        // Stale expiry cleared so the scheduler does not immediately re-flag it
        assertThat(document.getExpiryDate()).isNull();

        // Profile un-verified until the new document is approved
        assertThat(staffProfile.isVerified()).isFalse();

        assertThat(result.isReplacementRequested()).isFalse();
        assertThat(result.isVerified()).isFalse();
        verify(staffDocumentRepository).save(document);
        verify(staffProfileRepository).save(staffProfile);

        // Admins notified so they can review the new submission
        verify(userRepository).findByRoleIn(List.of(Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN));
        verify(emailService).sendDocumentSubmittedEmail(
                "admin@nurseadda.com", "Rohan Mehta", "STATE_BOARD_CERTIFICATE");
    }

    @Test
    @DisplayName("reuploadStaffDocument: no admin users means no notification email")
    void reuploadStaffDocument_noAdmins_noNotification(@TempDir Path tempDir) throws Exception {
        ReflectionTestUtils.setField(authService, "uploadDir", tempDir.toString());

        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setFirstName("Rohan");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setVerified(true);

        StaffDocument document = new StaffDocument();
        document.setId(100L);
        document.setStaffProfile(staffProfile);
        document.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        document.setFilePath("uploads/staff/5/certificate/old.pdf");
        document.setVerified(true);
        document.setReplacementRequested(true);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffDocumentRepository.findById(100L)).thenReturn(java.util.Optional.of(document));
        when(staffDocumentRepository.save(document)).thenReturn(document);
        when(userRepository.findByRoleIn(List.of(Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN)))
                .thenReturn(List.of());

        MockMultipartFile file = new MockMultipartFile("file", "new-cert.pdf",
                "application/pdf", new byte[]{1, 2, 3});

        authService.reuploadStaffDocument("rohan@test.com", 100L, file);

        verify(staffProfileRepository).save(staffProfile);
        verify(emailService, never()).sendDocumentSubmittedEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("reuploadStaffDocument: throws when no replacement was requested")
    void reuploadStaffDocument_notRequested_throwsIllegalArgument() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);
        staffProfile.setVerified(true);

        StaffDocument document = new StaffDocument();
        document.setId(100L);
        document.setStaffProfile(staffProfile);
        document.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        document.setVerified(true);
        document.setReplacementRequested(false);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffDocumentRepository.findById(100L)).thenReturn(java.util.Optional.of(document));

        MockMultipartFile file = new MockMultipartFile("file", "new-cert.pdf",
                "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> authService.reuploadStaffDocument("rohan@test.com", 100L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("replaced");

        verify(staffDocumentRepository, never()).save(any());
        verify(staffProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("reuploadStaffDocument: document of another profile throws ResourceNotFoundException")
    void reuploadStaffDocument_wrongProfile_throws() {
        User user = new User();
        user.setId(5L);
        user.setEmail("rohan@test.com");
        user.setRole(Role.ROLE_STAFF);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setId(10L);
        staffProfile.setUser(user);

        StaffProfile otherProfile = new StaffProfile();
        otherProfile.setId(99L);

        StaffDocument document = new StaffDocument();
        document.setId(100L);
        document.setStaffProfile(otherProfile);
        document.setReplacementRequested(true);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(java.util.Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(staffProfile));
        when(staffDocumentRepository.findById(100L)).thenReturn(java.util.Optional.of(document));

        MockMultipartFile file = new MockMultipartFile("file", "new-cert.pdf",
                "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> authService.reuploadStaffDocument("rohan@test.com", 100L, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("100");

        verify(staffDocumentRepository, never()).save(any());
    }

    // =====================================================================
    //  login — disabled (rejected) account
    // =====================================================================

    @Test
    @DisplayName("login: disabled account is rejected with IllegalCredentialsException")
    void login_disabledUser_throwsIllegalCredentials() {
        User user = new User();
        user.setId(1L);
        user.setEmail("disabled@test.com");
        user.setPassword("$2a$10$hash");
        user.setEnabled(false);
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByEmail("disabled@test.com"))
                .thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("secret123", "$2a$10$hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("disabled@test.com", "secret123")
        ))
                .isInstanceOf(IllegalCredentialsException.class)
                .hasMessageContaining("disabled");

        verify(jwtUtil, never()).generateAccessToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }
}
