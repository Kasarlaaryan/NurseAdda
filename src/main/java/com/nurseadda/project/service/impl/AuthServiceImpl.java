package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.BadRequestException;
import com.nurseadda.project.common.exception.DuplicateResourceException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.dto.request.RegisterRequest;
import com.nurseadda.project.dto.response.AuthResponse;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Set<Role> SELF_REGISTRABLE_ROLES = Set.of(Role.ROLE_STAFF, Role.ROLE_CLIENT);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("User", "phone", request.getPhone());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_STAFF;

        if (!SELF_REGISTRABLE_ROLES.contains(role)) {
            throw new BadRequestException(
                    "Self-registration is only allowed for STAFF and CLIENT. "
                            + "ADMIN and SUPER_ADMIN accounts must be created by a SUPER_ADMIN."
            );
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setEnabled(role == Role.ROLE_CLIENT);

        userRepository.save(user);

        return toAuthResponse(user,
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        return toAuthResponse(user,
                jwtUtil.generateAccessToken(user),
                jwtUtil.generateRefreshToken(user));
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

    private AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
