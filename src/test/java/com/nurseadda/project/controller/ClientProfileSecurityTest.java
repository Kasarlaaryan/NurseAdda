package com.nurseadda.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nurseadda.project.dto.request.ClientProfileRequest;
import com.nurseadda.project.security.JwtAuthenticationFilter;
import com.nurseadda.project.security.JwtUtil;
import com.nurseadda.project.security.TokenBlacklistService;
import com.nurseadda.project.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security tests verifying that only clients (ROLE_USER) can access
 * PUT /api/auth/client-profile.
 *
 * These tests validate the access-control rules defined in SecurityConfig:
 *   .requestMatchers(HttpMethod.PUT, "/api/auth/client-profile").hasRole("USER")
 *
 * Only negative cases (rejection) are tested here. The positive case
 * (ROLE_USER passes through to the controller) is verified in
 * AuthControllerTest using standalone MockMvc with a String principal.
 */
@WebMvcTest(AuthController.class)
@Import(ClientProfileSecurityTest.TestSecurityConfig.class)
class ClientProfileSecurityTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private ClientProfileRequest request;

    @BeforeEach
    void setUp() {
        request = new ClientProfileRequest("Riya", "Sharma", "9876543210");
    }

    /**
     * Test-specific SecurityFilterChain with a pass-through JWT filter.
     * Mirrors the production SecurityConfig's role-based access rules.
     */
    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter(
                JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
            return new JwtAuthenticationFilter(jwtUtil, tokenBlacklistService) {
                @Override
                protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }

                @Override
                protected boolean shouldNotFilter(HttpServletRequest request) {
                    return false;
                }
            };
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .httpBasic(httpBasic -> {})
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/api/auth/register-staff",
                                    "/api/auth/register-client",
                                    "/api/auth/login",
                                    "/api/auth/send-otp",
                                    "/api/auth/verify-otp",
                                    "/api/auth/resend-otp",
                                    "/api/auth/refresh",
                                    "/api/auth/forgot-password",
                                    "/api/auth/reset-password",
                                    "/uploads/**"
                            ).permitAll()
                            .requestMatchers(HttpMethod.PATCH, "/api/auth/users/*/unlock")
                            .hasAnyRole("ADMIN", "SUPER_ADMIN")
                            .requestMatchers("/api/auth/staff", "/api/auth/staff/*/verification")
                            .hasAnyRole("ADMIN", "SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/auth/client-profile")
                            .hasRole("USER")
                            .anyRequest().authenticated()
                    )
                    .build();
        }
    }

    // =====================================================================
    //  Security: PUT /api/auth/client-profile — hasRole("USER") only
    //
    //  Verifies the negative cases: unauthenticated and wrong-role requests
    //  are rejected by the security filter chain.
    //  Positive case (ROLE_USER passes) is tested in AuthControllerTest.
    // =====================================================================

    @Test
    @DisplayName("updateClientProfile: unauthenticated request returns 401 Unauthorized")
    void updateClientProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/auth/client-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).updateClientProfile(any(), any());
    }

    @Test
    @DisplayName("updateClientProfile: staff (ROLE_STAFF) is denied → 403 Forbidden")
    void updateClientProfile_staff_returns403() throws Exception {
        mockMvc.perform(put("/api/auth/client-profile")
                        .with(user("nurse@test.com").roles("STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(authService, never()).updateClientProfile(any(), any());
    }

    @Test
    @DisplayName("updateClientProfile: admin (ROLE_ADMIN) is denied → 403 Forbidden")
    void updateClientProfile_admin_returns403() throws Exception {
        mockMvc.perform(put("/api/auth/client-profile")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(authService, never()).updateClientProfile(any(), any());
    }

    @Test
    @DisplayName("updateClientProfile: super admin (ROLE_SUPER_ADMIN) is denied → 403 Forbidden")
    void updateClientProfile_superAdmin_returns403() throws Exception {
        mockMvc.perform(put("/api/auth/client-profile")
                        .with(user("superadmin@test.com").roles("SUPER_ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(authService, never()).updateClientProfile(any(), any());
    }

    @Test
    @DisplayName("updateClientProfile: client (ROLE_USER) passes security → not 401/403")
    void updateClientProfile_client_passesSecurity() throws Exception {
        mockMvc.perform(put("/api/auth/client-profile")
                        .with(user("user@test.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                                "ROLE_USER should pass security, but got HTTP " + status);
                    }
                });
    }
}
