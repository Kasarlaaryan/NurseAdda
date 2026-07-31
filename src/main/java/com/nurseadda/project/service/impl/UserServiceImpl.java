package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.BadRequestException;
import com.nurseadda.project.common.exception.DuplicateResourceException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.nurseadda.project.dto.request.UserRequest;
import com.nurseadda.project.dto.request.UserStatusRequest;
import com.nurseadda.project.dto.response.UserResponse;
import com.nurseadda.project.entity.Client;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final ClientRepository clientRepository;
    private final StaffDocumentRepository staffDocumentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(Role callerRole, UserRequest request) {
        validateUniqueCredentials(request.getEmail(), request.getPhone());

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_STAFF;
        checkPrivilegeToAssign(callerRole, role);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setEnabled(true);

        userRepository.save(user);
        createProfileIfNeeded(user);

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, Role callerRole, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        checkPrivilegeToManage(callerRole, user);

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && !request.getPhone().equals(user.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("User", "phone", request.getPhone());
        }

        Role oldRole = user.getRole();
        if (request.getRole() != null) {
            checkPrivilegeToAssign(callerRole, request.getRole());
            user.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);

        // If the role moved off a profile role, clean up the orphaned profile;
        // otherwise create the profile for the new role if missing.
        if (oldRole != user.getRole()) {
            deleteProfileIfUnused(user);
        }
        createProfileIfNeeded(user);

        return toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        staffProfileRepository.findByUserId(id).ifPresent(profile -> {
            staffDocumentRepository.deleteAll(
                    staffDocumentRepository.findByStaffProfileId(profile.getId()));
            staffProfileRepository.delete(profile);
        });
        clientRepository.findByUserId(id).ifPresent(clientRepository::delete);

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, Role callerRole, UserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        checkPrivilegeToManage(callerRole, user);

        user.setEnabled(request.getEnabled());
        userRepository.save(user);

        return toResponse(user);
    }

    private void checkPrivilegeToAssign(Role callerRole, Role targetRole) {
        if (callerRole != Role.ROLE_SUPER_ADMIN
                && (targetRole == Role.ROLE_SUPER_ADMIN || targetRole == Role.ROLE_ADMIN)) {
            throw new BadRequestException("Only a SUPER_ADMIN can create or assign ADMIN or SUPER_ADMIN roles");
        }
    }

    private void checkPrivilegeToManage(Role callerRole, User target) {
        if (callerRole != Role.ROLE_SUPER_ADMIN
                && (target.getRole() == Role.ROLE_SUPER_ADMIN || target.getRole() == Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("Only a SUPER_ADMIN can modify ADMIN or SUPER_ADMIN accounts");
        }
    }

    private void createProfileIfNeeded(User user) {
        if (user.getRole() == Role.ROLE_STAFF) {
            staffProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        StaffProfile profile = new StaffProfile();
                        profile.setUser(user);
                        return staffProfileRepository.save(profile);
                    });
        } else if (user.getRole() == Role.ROLE_CLIENT) {
            clientRepository.findByUserId(user.getId())
                    .orElseGet(() -> {
                        Client client = new Client();
                        client.setUser(user);
                        return clientRepository.save(client);
                    });
        }
    }

    private void deleteProfileIfUnused(User user) {
        if (user.getRole() != Role.ROLE_STAFF) {
            staffProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                staffDocumentRepository.deleteAll(
                        staffDocumentRepository.findByStaffProfileId(profile.getId()));
                staffProfileRepository.delete(profile);
            });
        }
        if (user.getRole() != Role.ROLE_CLIENT) {
            clientRepository.findByUserId(user.getId()).ifPresent(clientRepository::delete);
        }
    }

    private void validateUniqueCredentials(String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        if (phone != null && !phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("User", "phone", phone);
        }
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
