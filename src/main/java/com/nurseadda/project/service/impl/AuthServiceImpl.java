package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.DuplicateResourceException;
import com.nurseadda.project.common.exception.LoginException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.ClientRegisterRequest;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.StaffRegisterRequest;
import com.nurseadda.project.dto.response.AuthResponse;
import com.nurseadda.project.entity.Client;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${security.login.max-attempts:5}")
    private int maxFailedLoginAttempts;

    @Value("${security.login.lock-duration-minutes:30}")
    private long lockDurationMinutes;

    @Override
    @Transactional
    public AuthResponse registerStaff(StaffRegisterRequest request) {
        validateUniqueCredentials(request.getEmail(), request.getPhone());

        String[] nameParts = request.getFullName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(request.getPhone());
        user.setRole(Role.ROLE_STAFF);
        // Staff can log in immediately to complete their profile and upload
        // documents; the `verified` flag on StaffProfile gates assignment
        // eligibility until an admin approves their documents.
        user.setEnabled(true);

        userRepository.save(user);

        StaffProfile staffProfile = new StaffProfile();
        staffProfile.setUser(user);
        staffProfile.setStaffCategory(request.getStaffCategory());
        staffProfileRepository.save(staffProfile);

        return toAuthResponse(user,
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user));
    }

    @Override
    @Transactional
    public AuthResponse registerClient(ClientRegisterRequest request) {
        validateUniqueCredentials(request.getEmail(), request.getPhone());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName("");
        user.setLastName("");
        user.setPhone(request.getPhone());
        user.setRole(Role.ROLE_CLIENT);
        user.setEnabled(true);

        userRepository.save(user);

        // Create the client profile so the user can complete organization
        // details (name, type, contact person, address) after registration.
        Client client = new Client();
        client.setUser(user);
        clientRepository.save(client);

        return toAuthResponse(user,
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        // If a previous lock window has elapsed, unlock the account and give
        // the user a fresh set of attempts rather than re-locking on the next
        // single wrong password.
        if (user != null
                && user.getLockedUntil() != null
                && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User authenticatedUser = (User) authentication.getPrincipal();

            // A successful login clears any accumulated failed attempts.
            if (authenticatedUser.getFailedLoginAttempts() != 0
                    || authenticatedUser.getLockedUntil() != null) {
                authenticatedUser.setFailedLoginAttempts(0);
                authenticatedUser.setLockedUntil(null);
                userRepository.save(authenticatedUser);
            }

            return toAuthResponse(authenticatedUser,
                    jwtUtil.generateAccessToken(authenticatedUser),
                    jwtUtil.generateRefreshToken(authenticatedUser));
        } catch (BadCredentialsException ex) {
            if (user == null) {
                // Unknown account: rethrow so the caller gets a generic error
                // without leaking whether the account exists.
                throw ex;
            }

            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailedLoginAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
                userRepository.save(user);
                throw new LoginException(
                        "Account is locked. Please contact an administrator",
                        0,
                        lockoutSeconds(user));
            }

            userRepository.save(user);
            throw new LoginException(
                    "Invalid email or password",
                    maxFailedLoginAttempts - attempts,
                    null);
        } catch (LockedException ex) {
            throw new LoginException(
                    "Account is locked. Please contact an administrator",
                    0,
                    lockoutSeconds(user));
        }
    }

    private long lockoutSeconds(User user) {
        if (user != null && user.getLockedUntil() != null) {
            return Math.max(Duration.between(LocalDateTime.now(), user.getLockedUntil()).getSeconds(), 0);
        }
        return lockDurationMinutes * 60;
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtUtil.retriveEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return toAuthResponse(user,
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user));
    }

    @Override
    public AuthResponse getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));

        return toAuthResponse(user, null, null);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    private void validateUniqueCredentials(String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        if (phone != null && !phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("User", "phone", phone);
        }
    }

    private AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.AuthResponseBuilder builder = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName());

        if (user.getRole() == Role.ROLE_STAFF) {
            staffProfileRepository.findByUserId(user.getId())
                    .ifPresent(profile -> builder
                            .staffCategory(profile.getStaffCategory())
                            .verified(profile.isVerified()));
        }

        return builder.build();
    }
}
