package com.nurseadda.project.service.impl;

import com.nurseadda.project.common.exception.LoginException;
import com.nurseadda.project.common.exception.ResourceNotFoundException;
import com.nurseadda.project.dto.request.LoginRequest;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.Role;
import com.nurseadda.project.repository.ClientRepository;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import com.nurseadda.project.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLockoutTest {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 30;
    private static final String EMAIL = "staff@example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private StaffProfileRepository staffProfileRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository, staffProfileRepository, clientRepository,
                passwordEncoder, authenticationManager, jwtUtil);
        ReflectionTestUtils.setField(authService, "maxFailedLoginAttempts", MAX_ATTEMPTS);
        ReflectionTestUtils.setField(authService, "lockDurationMinutes", LOCK_MINUTES);
    }

    @Test
    void wrongPassword_locksAccountAfterMaxAttempts() {
        User user = user(0, null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Attempts 1..4: not locked, remaining attempts feedback provided.
        for (int attempt = 1; attempt < MAX_ATTEMPTS; attempt++) {
            LoginException ex = assertThrows(LoginException.class, () -> authService.login(loginRequest()));
            assertThat(ex.getRemainingAttempts()).isEqualTo(MAX_ATTEMPTS - attempt);
            assertThat(ex.getLockoutSeconds()).isNull();
            assertThat(user.getFailedLoginAttempts()).isEqualTo(attempt);
            assertThat(user.getLockedUntil()).isNull();
        }

        // 5th failure: account becomes locked with a countdown.
        LoginException ex = assertThrows(LoginException.class, () -> authService.login(loginRequest()));
        assertThat(ex.getRemainingAttempts()).isZero();
        assertThat(ex.getLockoutSeconds()).isNotNull().isPositive();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(MAX_ATTEMPTS);
        assertThat(user.getLockedUntil()).isNotNull();
    }

    @Test
    void login_whileLocked_returnsCountdownAndKeepsCounter() {
        User user = user(MAX_ATTEMPTS, LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(LockedException.class);

        LoginException ex = assertThrows(LoginException.class, () -> authService.login(loginRequest()));

        assertThat(ex.getRemainingAttempts()).isZero();
        assertThat(ex.getLockoutSeconds()).isNotNull().isPositive();
        // Counter is not incremented further while the account is locked.
        assertThat(user.getFailedLoginAttempts()).isEqualTo(MAX_ATTEMPTS);
        verify(userRepository, never()).save(user);
    }

    @Test
    void login_afterLockExpired_resetsCounterAndGrantsFreshAttempts() {
        User user = user(MAX_ATTEMPTS, LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        LoginException ex = assertThrows(LoginException.class, () -> authService.login(loginRequest()));

        // Expired lock cleared, counter reset to 0, then one wrong attempt counted.
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(ex.getRemainingAttempts()).isEqualTo(MAX_ATTEMPTS - 1);
    }

    @Test
    void successfulLogin_clearsFailedAttemptsAndLock() {
        User user = user(3, null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        authService.login(loginRequest());

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void unlockUser_clearsFailedAttemptsAndLock() {
        User user = user(MAX_ATTEMPTS, LocalDateTime.now().plusMinutes(5));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.unlockUser(1L);

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void unlockUser_unknownUser_throws404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.unlockUser(99L));
    }

    @Test
    void login_unknownEmail_keepsGenericErrorWithoutSaving() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest()));
        verify(userRepository, never()).save(any(User.class));
    }

    private User user(int failedAttempts, LocalDateTime lockedUntil) {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_CLIENT);
        user.setEnabled(true);
        user.setFailedLoginAttempts(failedAttempts);
        user.setLockedUntil(lockedUntil);
        return user;
    }

    private LoginRequest loginRequest() {
        return new LoginRequest(EMAIL, "wrong-password");
    }
}
