package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.common.exception.UserAlreadyExistException;
import com.nurseadda.project.common.exception.UserNotFoundException;
import com.nurseadda.project.dto.request.AdminCreateUserRequest;
import com.nurseadda.project.dto.request.AdminUpdateStaffProfileRequest;
import com.nurseadda.project.dto.request.AdminUpdateUserRequest;
import com.nurseadda.project.dto.response.StaffDocumentResponseDto;
import com.nurseadda.project.dto.response.StaffProfileResponseDto;
import com.nurseadda.project.dto.response.UserResponseDto;
import com.nurseadda.project.entity.Client;
import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.enums.StaffDocumentType;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.AuthService;
import com.nurseadda.project.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffProfileRepository staffProfileRepository;

    @Mock
    private StaffDocumentRepository staffDocumentRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User superAdmin;

    @BeforeEach
    void setUp() {
        superAdmin = new User();
        superAdmin.setId(1L);
        superAdmin.setEmail("root@nurseadda.com");
        superAdmin.setRole(Role.ROLE_SUPER_ADMIN);
    }

    private User user(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    private UserResponseDto dto(Long id, String email, Role role) {
        return UserResponseDto.builder()
                .id(id)
                .email(email)
                .role(role)
                .enabled(true)
                .build();
    }

    // =====================================================================
    //  getAllUsers
    // =====================================================================

    @Test
    @DisplayName("getAllUsers: filters by role when role is provided")
    void getAllUsers_withRole_filtersByRole() {
        Page<User> userPage = new PageImpl<>(
                List.of(user(2L, "staff@test.com", Role.ROLE_STAFF)),
                PageRequest.of(0, 10),
                1
        );
        when(userRepository.findByRole(Role.ROLE_STAFF, PageRequest.of(0, 10))).thenReturn(userPage);
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class)))
                .thenReturn(dto(2L, "staff@test.com", Role.ROLE_STAFF));

        Page<UserResponseDto> result = adminService.getAllUsers(PageRequest.of(0, 10), Role.ROLE_STAFF);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("staff@test.com");
        verify(userRepository).findByRole(Role.ROLE_STAFF, PageRequest.of(0, 10));
        verify(userRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("getAllUsers: returns all users when role is null")
    void getAllUsers_noRole_returnsAll() {
        Page<User> userPage = new PageImpl<>(List.of(user(2L, "a@test.com", Role.ROLE_USER)),
                PageRequest.of(0, 10), 1);
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(userPage);
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class)))
                .thenReturn(dto(2L, "a@test.com", Role.ROLE_USER));

        adminService.getAllUsers(PageRequest.of(0, 10), null);

        verify(userRepository).findAll(PageRequest.of(0, 10));
        verify(userRepository, never()).findByRole(any(), any());
    }

    @Test
    @DisplayName("getUserById: user not found throws UserNotFoundException")
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    // =====================================================================
    //  createUser
    // =====================================================================

    @Test
    @DisplayName("createUser: creates a staff account with staff profile")
    void createUser_staffRole_createsProfile() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Rohan", "Mehta", "rohan@test.com", "9876543210",
                "secret123", Role.ROLE_STAFF, "ICU Nurse"
        );

        when(userRepository.existsByEmail("rohan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ROLE_STAFF);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$10$hash");
        assertThat(userCaptor.getValue().isEmailVerified()).isTrue();
        assertThat(userCaptor.getValue().isEnabled()).isTrue();

        ArgumentCaptor<StaffProfile> profileCaptor = ArgumentCaptor.forClass(StaffProfile.class);
        verify(staffProfileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getStaffCategory()).isEqualTo("ICU Nurse");
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: creates a client account for ROLE_USER")
    void createUser_userRole_createsClient() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Client", "User", "client@test.com", "9876543210",
                "secret123", Role.ROLE_USER, null
        );

        when(userRepository.existsByEmail("client@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        adminService.createUser(request);

        verify(staffProfileRepository, never()).save(any());
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        assertThat(clientCaptor.getValue().getUser().getEmail()).isEqualTo("client@test.com");
    }

    @Test
    @DisplayName("createUser: duplicate email throws UserAlreadyExistException")
    void createUser_duplicateEmail_throws() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Rohan", "Mehta", "rohan@test.com", "9876543210",
                "secret123", Role.ROLE_STAFF, "ICU Nurse"
        );
        when(userRepository.existsByEmail("rohan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(request))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: staff without staffCategory throws IllegalArgumentException")
    void createUser_staffWithoutCategory_throws() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Rohan", "Mehta", "rohan@test.com", "9876543210",
                "secret123", Role.ROLE_STAFF, null
        );
        when(userRepository.existsByEmail("rohan@test.com")).thenReturn(false);

        assertThatThrownBy(() -> adminService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Staff category");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser: cannot create a SUPER_ADMIN account")
    void createUser_superAdminRole_throws() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "Root", "User", "root2@test.com", "9876543210",
                "secret123", Role.ROLE_SUPER_ADMIN, null
        );
        when(userRepository.existsByEmail("root2@test.com")).thenReturn(false);

        assertThatThrownBy(() -> adminService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPER_ADMIN");

        verify(userRepository, never()).save(any());
    }

    // =====================================================================
    //  updateUser
    // =====================================================================

    @Test
    @DisplayName("updateUser: updates name, phone, email and re-encodes password")
    void updateUser_validRequest_updatesFields() {
        User user = user(2L, "old@test.com", Role.ROLE_USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$newhash");
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserResponseDto.class)).thenReturn(dto(2L, "new@test.com", Role.ROLE_USER));

        adminService.updateUser(2L, new AdminUpdateUserRequest(
                "New", "Name", "new@test.com", "9999999999", "newpass123", null, null
        ), "root@nurseadda.com");

        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getPhone()).isEqualTo("9999999999");
        assertThat(user.getPassword()).isEqualTo("$2a$10$newhash");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser: changing email to an existing one throws UserAlreadyExistException")
    void updateUser_emailConflict_throws() {
        User user = user(2L, "old@test.com", Role.ROLE_USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.updateUser(2L,
                new AdminUpdateUserRequest(null, null, "taken@test.com", null, null, null, null),
                "root@nurseadda.com"))
                .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser: cannot change your own role")
    void updateUser_changeOwnRole_throws() {
        User user = user(1L, "root@nurseadda.com", Role.ROLE_SUPER_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.updateUser(1L,
                new AdminUpdateUserRequest(null, null, null, null, null, Role.ROLE_ADMIN, null),
                "root@nurseadda.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own role");
    }

    @Test
    @DisplayName("updateUser: cannot modify another SUPER_ADMIN account")
    void updateUser_modifyOtherSuperAdmin_throws() {
        User other = user(3L, "root2@nurseadda.com", Role.ROLE_SUPER_ADMIN);
        when(userRepository.findById(3L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> adminService.updateUser(3L,
                new AdminUpdateUserRequest("X", null, null, null, null, null, null),
                "root@nurseadda.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPER_ADMIN");
    }

    @Test
    @DisplayName("updateUser: promoting a user to staff creates the staff profile")
    void updateUser_promoteToStaff_createsProfile() {
        User user = user(2L, "rohan@test.com", Role.ROLE_USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        adminService.updateUser(2L,
                new AdminUpdateUserRequest(null, null, null, null, null, Role.ROLE_STAFF, "ICU Nurse"),
                "root@nurseadda.com");

        assertThat(user.getRole()).isEqualTo(Role.ROLE_STAFF);
        ArgumentCaptor<StaffProfile> captor = ArgumentCaptor.forClass(StaffProfile.class);
        verify(staffProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getStaffCategory()).isEqualTo("ICU Nurse");
    }

    @Test
    @DisplayName("updateUser: demoting staff deletes the staff profile and documents")
    void updateUser_demoteFromStaff_deletesProfile(@TempDir Path tempDir) {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setStaffCategory("ICU Nurse");

        Path file = tempDir.resolve("cert.pdf");
        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(profile);
        doc.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        doc.setFilePath(file.toString());

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findByStaffProfileId(10L)).thenReturn(List.of(doc));
        when(userRepository.save(user)).thenReturn(user);

        adminService.updateUser(2L,
                new AdminUpdateUserRequest(null, null, null, null, null, Role.ROLE_USER, null),
                "root@nurseadda.com");

        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);
        verify(staffDocumentRepository).deleteAllByStaffProfileId(10L);
        verify(staffProfileRepository).delete(profile);
        verify(clientRepository).save(any(Client.class));
    }

    // =====================================================================
    //  deleteUser
    // =====================================================================

    @Test
    @DisplayName("deleteUser: deletes user, staff profile and documents")
    void deleteUser_validUser_deletesEverything(@TempDir Path tempDir) throws Exception {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);

        Path file = tempDir.resolve("cert.pdf");
        Files.writeString(file, "data");
        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(profile);
        doc.setFilePath(file.toString());

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findByStaffProfileId(10L)).thenReturn(List.of(doc));

        adminService.deleteUser(2L, "root@nurseadda.com");

        verify(staffDocumentRepository).deleteAllByStaffProfileId(10L);
        verify(staffProfileRepository).delete(profile);
        verify(userRepository).delete(user);
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    @DisplayName("deleteUser: cannot delete your own account")
    void deleteUser_self_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> adminService.deleteUser(1L, "root@nurseadda.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own account");

        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteUser: cannot delete a SUPER_ADMIN account")
    void deleteUser_superAdmin_throws() {
        User other = user(3L, "root2@nurseadda.com", Role.ROLE_SUPER_ADMIN);
        when(userRepository.findById(3L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> adminService.deleteUser(3L, "root@nurseadda.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPER_ADMIN");
    }

    // =====================================================================
    //  setUserEnabled (reject user)
    // =====================================================================

    @Test
    @DisplayName("setUserEnabled: disables an account")
    void setUserEnabled_disable_updatesFlag() {
        User user = user(2L, "rohan@test.com", Role.ROLE_USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserResponseDto.class)).thenReturn(dto(2L, "rohan@test.com", Role.ROLE_USER));

        adminService.setUserEnabled(2L, false, "root@nurseadda.com");

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("setUserEnabled: re-enabling clears previous lockout state")
    void setUserEnabled_reEnable_clearsLockout() {
        User user = user(2L, "rohan@test.com", Role.ROLE_USER);
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(LocalDate.now().atStartOfDay().plusHours(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(dto(2L, "rohan@test.com", Role.ROLE_USER));

        adminService.setUserEnabled(2L, true, "root@nurseadda.com");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("setUserEnabled: cannot disable your own account")
    void setUserEnabled_self_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> adminService.setUserEnabled(1L, false, "root@nurseadda.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own account");
    }

    @Test
    @DisplayName("setUserEnabled: cannot disable another SUPER_ADMIN")
    void setUserEnabled_superAdmin_throws() {
        User other = user(3L, "root2@nurseadda.com", Role.ROLE_SUPER_ADMIN);
        when(userRepository.findById(3L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> adminService.setUserEnabled(3L, false, "root@nurseadda.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPER_ADMIN");
    }

    // =====================================================================
    //  Staff (delegation)
    // =====================================================================

    @Test
    @DisplayName("getAllStaffProfiles: delegates to AuthService")
    void getAllStaffProfiles_delegates() {
        Page<StaffProfileResponseDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(authService.getAllStaffProfiles(PageRequest.of(0, 10))).thenReturn(page);

        Page<StaffProfileResponseDto> result = adminService.getAllStaffProfiles(PageRequest.of(0, 10));

        assertThat(result).isSameAs(page);
    }

    @Test
    @DisplayName("getStaffProfileByUserId: delegates using the user email")
    void getStaffProfileByUserId_delegates() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);
        StaffProfileResponseDto response = StaffProfileResponseDto.builder()
                .id(10L).email("rohan@test.com").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(authService.getStaffProfile("rohan@test.com")).thenReturn(response);

        StaffProfileResponseDto result = adminService.getStaffProfileByUserId(2L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("updateStaffProfileByAdmin: updates profile fields")
    void updateStaffProfileByAdmin_updatesFields() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setStaffCategory("ICU Nurse");

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffProfileRepository.save(profile)).thenReturn(profile);

        StaffProfileResponseDto response = StaffProfileResponseDto.builder()
                .id(10L).staffCategory("Ward Nurse").build();
        when(authService.getStaffProfile("rohan@test.com")).thenReturn(response);

        StaffProfileResponseDto result = adminService.updateStaffProfileByAdmin(2L,
                new AdminUpdateStaffProfileRequest(
                        "Ward Nurse", "123456789012",
                        LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 15)
                ));

        assertThat(profile.getStaffCategory()).isEqualTo("Ward Nurse");
        assertThat(profile.getAadharCardNumber()).isEqualTo("123456789012");
        assertThat(profile.getLicenseValidityDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("updateStaffProfileByAdmin: profile not found throws ResourceNotFoundException")
    void updateStaffProfileByAdmin_notFound_throws() {
        when(staffProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateStaffProfileByAdmin(99L,
                new AdminUpdateStaffProfileRequest(null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("verifyStaffProfile: delegates to AuthService")
    void verifyStaffProfile_delegates() {
        StaffProfileResponseDto response = StaffProfileResponseDto.builder()
                .id(10L).verified(true).build();
        when(authService.verifyStaffProfile(2L, true)).thenReturn(response);

        StaffProfileResponseDto result = adminService.verifyStaffProfile(2L, true);

        assertThat(result).isSameAs(response);
        verify(authService).verifyStaffProfile(2L, true);
    }

    // =====================================================================
    //  Documents
    // =====================================================================

    @Test
    @DisplayName("getStaffDocuments: returns documents of the staff profile")
    void getStaffDocuments_returnsDocs() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);

        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(profile);
        doc.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        doc.setFileName("cert.pdf");
        doc.setFilePath("uploads/staff/2/certificate/cert.pdf");
        doc.setVerified(true);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findByStaffProfileId(10L)).thenReturn(List.of(doc));

        List<StaffDocumentResponseDto> result = adminService.getStaffDocuments(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(0).getDocumentType()).isEqualTo(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        assertThat(result.get(0).isVerified()).isTrue();
    }

    @Test
    @DisplayName("verifyStaffDocument: verifying last document auto-verifies the profile and emails")
    void verifyStaffDocument_allVerified_verifiesProfile() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);
        user.setFirstName("Rohan");

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setVerified(false);

        StaffDocument cert = new StaffDocument();
        cert.setId(100L);
        cert.setStaffProfile(profile);
        cert.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        cert.setVerified(false);

        StaffDocument education = new StaffDocument();
        education.setId(101L);
        education.setStaffProfile(profile);
        education.setDocumentType(StaffDocumentType.EDUCATIONAL_CERTIFICATE);
        education.setVerified(true);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(cert));
        when(staffDocumentRepository.findByStaffProfileId(10L)).thenReturn(List.of(cert, education));

        StaffDocumentResponseDto result = adminService.verifyStaffDocument(2L, 100L, true);

        assertThat(result.isVerified()).isTrue();
        assertThat(profile.isVerified()).isTrue();
        verify(staffProfileRepository).save(profile);
        verify(emailService).sendProfileVerifiedEmail("rohan@test.com", "Rohan");
        verify(emailService, never()).sendProfileRejectedEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("verifyStaffDocument: rejecting a document rejects the whole profile and emails")
    void verifyStaffDocument_reject_rejectsProfile() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);
        user.setFirstName("Rohan");

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setVerified(true);

        StaffDocument cert = new StaffDocument();
        cert.setId(100L);
        cert.setStaffProfile(profile);
        cert.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        cert.setVerified(true);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(cert));

        adminService.verifyStaffDocument(2L, 100L, false);

        assertThat(cert.isVerified()).isFalse();
        assertThat(profile.isVerified()).isFalse();
        verify(staffProfileRepository).save(profile);
        verify(emailService).sendProfileRejectedEmail("rohan@test.com", "Rohan");
        verify(emailService, never()).sendProfileVerifiedEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("verifyStaffDocument: verifying one of several docs does not verify profile yet")
    void verifyStaffDocument_partial_doesNotVerifyProfile() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setVerified(false);

        StaffDocument cert = new StaffDocument();
        cert.setId(100L);
        cert.setStaffProfile(profile);
        cert.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        cert.setVerified(false);

        StaffDocument education = new StaffDocument();
        education.setId(101L);
        education.setStaffProfile(profile);
        education.setDocumentType(StaffDocumentType.EDUCATIONAL_CERTIFICATE);
        education.setVerified(false);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(cert));
        when(staffDocumentRepository.findByStaffProfileId(10L)).thenReturn(List.of(cert, education));

        adminService.verifyStaffDocument(2L, 100L, true);

        assertThat(cert.isVerified()).isTrue();
        assertThat(profile.isVerified()).isFalse();
        verify(staffProfileRepository, never()).save(profile);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("verifyStaffDocument: document of another profile throws ResourceNotFoundException")
    void verifyStaffDocument_wrongProfile_throws() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);

        StaffProfile otherProfile = new StaffProfile();
        otherProfile.setId(99L);

        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(otherProfile);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> adminService.verifyStaffDocument(2L, 100L, true))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(staffDocumentRepository, never()).save(any());
    }

    // =====================================================================
    //  requestDocumentReplacement
    // =====================================================================

    @Test
    @DisplayName("requestDocumentReplacement: requesting sets the flag, reason and expiry, and emails staff")
    void requestDocumentReplacement_request_setsFlagsAndEmails() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);
        user.setFirstName("Rohan");

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);

        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(profile);
        doc.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        doc.setVerified(true);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(staffDocumentRepository.save(doc)).thenReturn(doc);

        StaffDocumentResponseDto result = adminService.requestDocumentReplacement(
                2L, 100L, true, "Certificate expired", LocalDate.of(2025, 6, 30));

        assertThat(doc.isReplacementRequested()).isTrue();
        assertThat(doc.getRequestReason()).isEqualTo("Certificate expired");
        assertThat(doc.getExpiryDate()).isEqualTo(LocalDate.of(2025, 6, 30));
        assertThat(result.isReplacementRequested()).isTrue();
        assertThat(result.getRequestReason()).isEqualTo("Certificate expired");
        assertThat(result.getExpiryDate()).isEqualTo(LocalDate.of(2025, 6, 30));
        verify(staffDocumentRepository).save(doc);
        verify(emailService).sendDocumentReplacementRequestEmail(
                "rohan@test.com", "Rohan", "STATE_BOARD_CERTIFICATE", "Certificate expired");
    }

    @Test
    @DisplayName("requestDocumentReplacement: cancelling clears the flag and reason, no email")
    void requestDocumentReplacement_cancel_clearsFlags() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);
        user.setFirstName("Rohan");

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);

        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(profile);
        doc.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        doc.setReplacementRequested(true);
        doc.setRequestReason("Certificate expired");

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(doc));
        when(staffDocumentRepository.save(doc)).thenReturn(doc);

        StaffDocumentResponseDto result = adminService.requestDocumentReplacement(
                2L, 100L, false, null, null);

        assertThat(doc.isReplacementRequested()).isFalse();
        assertThat(doc.getRequestReason()).isNull();
        assertThat(result.isReplacementRequested()).isFalse();
        assertThat(result.getRequestReason()).isNull();
        verify(staffDocumentRepository).save(doc);
        verify(emailService, never()).sendDocumentReplacementRequestEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("requestDocumentReplacement: document of another profile throws ResourceNotFoundException")
    void requestDocumentReplacement_wrongProfile_throws() {
        User user = user(2L, "rohan@test.com", Role.ROLE_STAFF);

        StaffProfile profile = new StaffProfile();
        profile.setId(10L);
        profile.setUser(user);

        StaffProfile otherProfile = new StaffProfile();
        otherProfile.setId(99L);

        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(otherProfile);

        when(staffProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));
        when(staffDocumentRepository.findById(100L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> adminService.requestDocumentReplacement(
                2L, 100L, true, "expired", null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(staffDocumentRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("requestDocumentReplacement: staff profile not found throws ResourceNotFoundException")
    void requestDocumentReplacement_profileNotFound_throws() {
        when(staffProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.requestDocumentReplacement(
                99L, 100L, true, "expired", null))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(emailService);
    }
}
