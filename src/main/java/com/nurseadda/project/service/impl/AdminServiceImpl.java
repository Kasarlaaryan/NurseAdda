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
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.AdminService;
import com.nurseadda.project.service.AuthService;
import com.nurseadda.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthService authService;
    private final EmailService emailService;

    // =====================================================================
    //  Users
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable, Role role) {
        Page<User> users = (role == null)
                ? userRepository.findAll(pageable)
                : userRepository.findByRole(role, pageable);

        return users.map(user -> modelMapper.map(user, UserResponseDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) throws UserNotFoundException {
        return modelMapper.map(findUser(id), UserResponseDto.class);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(AdminCreateUserRequest request) throws UserAlreadyExistException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException(
                    "User already exists with email : " + request.getEmail()
            );
        }

        if (request.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot create a SUPER_ADMIN account");
        }

        if (request.getRole() == Role.ROLE_STAFF
                && (request.getStaffCategory() == null || request.getStaffCategory().isBlank())) {
            throw new IllegalArgumentException("Staff category is required when creating a staff account");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setEmailVerified(true);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.ROLE_STAFF) {
            StaffProfile staffProfile = new StaffProfile();
            staffProfile.setUser(savedUser);
            staffProfile.setStaffCategory(request.getStaffCategory());
            staffProfileRepository.save(staffProfile);
        } else if (request.getRole() == Role.ROLE_USER) {
            Client client = new Client();
            client.setUser(savedUser);
            clientRepository.save(client);
        }

        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, AdminUpdateUserRequest request, String actingEmail)
            throws UserNotFoundException, UserAlreadyExistException {
        User user = findUser(id);

        if (user.getRole() == Role.ROLE_SUPER_ADMIN
                && !actingEmail.equals(user.getEmail())) {
            throw new IllegalArgumentException("Cannot modify another SUPER_ADMIN account");
        }

        if (actingEmail.equals(user.getEmail())
                && request.getRole() != null
                && request.getRole() != user.getRole()) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistException(
                        "User already exists with email : " + request.getEmail()
                );
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            applyRoleChange(user, request.getRole(), request.getStaffCategory());
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String actingEmail) throws UserNotFoundException {
        User user = findUser(id);

        if (user.getEmail().equals(actingEmail)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot delete a SUPER_ADMIN account");
        }

        deleteStaffData(user.getId());
        clientRepository.findByUserId(user.getId()).ifPresent(clientRepository::delete);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDto setUserEnabled(Long id, boolean enabled, String actingEmail)
            throws UserNotFoundException {
        User user = findUser(id);

        if (user.getEmail().equals(actingEmail)) {
            throw new IllegalArgumentException("You cannot change the status of your own account");
        }

        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot change the status of a SUPER_ADMIN account");
        }

        user.setEnabled(enabled);
        if (enabled) {
            // Re-enabling an account also clears any prior lockout state
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    // =====================================================================
    //  Staff
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<StaffProfileResponseDto> getAllStaffProfiles(Pageable pageable) {
        return authService.getAllStaffProfiles(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponseDto getStaffProfileByUserId(Long userId)
            throws UserNotFoundException, ResourceNotFoundException {
        User user = findUser(userId);
        return authService.getStaffProfile(user.getEmail());
    }

    @Override
    @Transactional
    public StaffProfileResponseDto updateStaffProfileByAdmin(Long userId, AdminUpdateStaffProfileRequest request)
            throws ResourceNotFoundException {
        StaffProfile staffProfile = staffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user id : " + userId
                ));

        if (request.getStaffCategory() != null && !request.getStaffCategory().isBlank()) {
            staffProfile.setStaffCategory(request.getStaffCategory());
        }

        if (request.getAadharCardNumber() != null && !request.getAadharCardNumber().isBlank()) {
            staffProfile.setAadharCardNumber(request.getAadharCardNumber());
        }

        if (request.getLicenseValidityDate() != null) {
            staffProfile.setLicenseValidityDate(request.getLicenseValidityDate());
        }

        if (request.getLicenseRenewalDate() != null) {
            staffProfile.setLicenseRenewalDate(request.getLicenseRenewalDate());
        }

        staffProfileRepository.save(staffProfile);

        return authService.getStaffProfile(staffProfile.getUser().getEmail());
    }

    @Override
    public StaffProfileResponseDto verifyStaffProfile(Long userId, boolean verified)
            throws ResourceNotFoundException {
        return authService.verifyStaffProfile(userId, verified);
    }

    // =====================================================================
    //  Documents
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<StaffDocumentResponseDto> getStaffDocuments(Long userId) throws ResourceNotFoundException {
        StaffProfile staffProfile = staffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user id : " + userId
                ));

        return staffDocumentRepository.findByStaffProfileId(staffProfile.getId()).stream()
                .map(this::toDocumentDto)
                .toList();
    }

    @Override
    @Transactional
    public StaffDocumentResponseDto verifyStaffDocument(Long userId, Long documentId, boolean verified)
            throws ResourceNotFoundException {
        StaffProfile staffProfile = staffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user id : " + userId
                ));

        StaffDocument document = staffDocumentRepository.findById(documentId)
                .filter(d -> d.getStaffProfile().getId().equals(staffProfile.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id : " + documentId
                                + " for user id : " + userId
                ));

        document.setVerified(verified);
        staffDocumentRepository.save(document);

        if (verified) {
            // Auto-verify the profile when every uploaded document is verified
            List<StaffDocument> allDocuments = staffDocumentRepository.findByStaffProfileId(staffProfile.getId());
            boolean allVerified = !allDocuments.isEmpty()
                    && allDocuments.stream().allMatch(StaffDocument::isVerified);

            if (allVerified && !staffProfile.isVerified()) {
                staffProfile.setVerified(true);
                staffProfileRepository.save(staffProfile);
                emailService.sendProfileVerifiedEmail(
                        staffProfile.getUser().getEmail(),
                        staffProfile.getUser().getFirstName()
                );
            }
        } else if (staffProfile.isVerified()) {
            // Rejecting any document rejects the whole profile
            staffProfile.setVerified(false);
            staffProfileRepository.save(staffProfile);
            emailService.sendProfileRejectedEmail(
                    staffProfile.getUser().getEmail(),
                    staffProfile.getUser().getFirstName()
            );
        }

        return toDocumentDto(document);
    }

    @Override
    @Transactional
    public StaffDocumentResponseDto requestDocumentReplacement(
            Long userId, Long documentId, boolean requested, String reason, LocalDate expiryDate
    ) throws ResourceNotFoundException {
        StaffProfile staffProfile = staffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff profile not found for user id : " + userId
                ));

        StaffDocument document = staffDocumentRepository.findById(documentId)
                .filter(d -> d.getStaffProfile().getId().equals(staffProfile.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id : " + documentId
                                + " for user id : " + userId
                ));

        if (requested) {
            document.setReplacementRequested(true);
            document.setRequestReason(reason);
            if (expiryDate != null) {
                document.setExpiryDate(expiryDate);
            }
            emailService.sendDocumentReplacementRequestEmail(
                    staffProfile.getUser().getEmail(),
                    staffProfile.getUser().getFirstName(),
                    document.getDocumentType().name(),
                    reason
            );
        } else {
            // Cancelling the request
            document.setReplacementRequested(false);
            document.setRequestReason(null);
        }

        staffDocumentRepository.save(document);
        return toDocumentDto(document);
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private User findUser(Long id) throws UserNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id : " + id
                ));
    }

    private void applyRoleChange(User user, Role targetRole, String staffCategory) {
        if (targetRole == Role.ROLE_SUPER_ADMIN) {
            throw new IllegalArgumentException("Cannot assign the SUPER_ADMIN role");
        }

        if (targetRole == user.getRole()) {
            return;
        }

        if (targetRole == Role.ROLE_STAFF) {
            if (staffCategory == null || staffCategory.isBlank()) {
                throw new IllegalArgumentException(
                        "Staff category is required when assigning the staff role"
                );
            }

            StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        StaffProfile profile = new StaffProfile();
                        profile.setUser(user);
                        return profile;
                    });
            staffProfile.setStaffCategory(staffCategory);
            staffProfileRepository.save(staffProfile);

            clientRepository.findByUserId(user.getId()).ifPresent(clientRepository::delete);
        } else {
            // Leaving the staff role removes the staff profile and its documents
            if (user.getRole() == Role.ROLE_STAFF) {
                deleteStaffData(user.getId());
            }

            if (targetRole == Role.ROLE_USER) {
                Client client = clientRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            Client newClient = new Client();
                            newClient.setUser(user);
                            return newClient;
                        });
                clientRepository.save(client);
            } else {
                // Non-user roles (e.g. ADMIN) do not keep a client record
                clientRepository.findByUserId(user.getId()).ifPresent(clientRepository::delete);
            }
        }

        user.setRole(targetRole);
    }

    private void deleteStaffData(Long userId) {
        staffProfileRepository.findByUserId(userId).ifPresent(staffProfile -> {
            List<StaffDocument> documents = staffDocumentRepository.findByStaffProfileId(staffProfile.getId());
            deleteFiles(documents);
            staffDocumentRepository.deleteAllByStaffProfileId(staffProfile.getId());
            staffProfileRepository.delete(staffProfile);
        });
    }

    private void deleteFiles(List<StaffDocument> documents) {
        for (StaffDocument document : documents) {
            if (document.getFilePath() == null || document.getFilePath().isBlank()) {
                continue;
            }
            try {
                Files.deleteIfExists(Paths.get(document.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", document.getFilePath(), e);
            }
        }
    }

    private StaffDocumentResponseDto toDocumentDto(StaffDocument document) {
        return StaffDocumentResponseDto.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .filePath(document.getFilePath())
                .verified(document.isVerified())
                .expiryDate(document.getExpiryDate())
                .replacementRequested(document.isReplacementRequested())
                .requestReason(document.getRequestReason())
                .build();
    }
}
