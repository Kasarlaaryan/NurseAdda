package com.nurseadda.project.config;

import com.nurseadda.project.security.JwtAuthenticationFilter;
import com.nurseadda.project.security.ProfileVerificationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ProfileVerificationFilter profileVerificationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register-staff",
                                "/api/auth/register-client",
                                "/api/auth/login",
                                "/api/auth/send-otp",
                                "/api/auth/verify-otp",
                                "/uploads/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/staff", "/api/auth/staff/*/verification").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/staff/*/documents")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/admin/staff/*/documents/*/verification",
                                "/api/admin/staff/*/documents/*/replacement-request"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(profileVerificationFilter, JwtAuthenticationFilter.class)
                .build();
    }
}
