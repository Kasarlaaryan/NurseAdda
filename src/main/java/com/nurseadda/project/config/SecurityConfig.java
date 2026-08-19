package com.nurseadda.project.config;

import com.nurseadda.project.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                                "/api/auth/resend-otp",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/uploads/**"
                        ).permitAll()
                        // Auth profile endpoints - role-based
                        .requestMatchers(HttpMethod.PATCH, "/api/auth/users/*/unlock")
                                .hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/auth/staff", "/api/auth/staff/*/verification").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/client-profile").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/auth/client-profile").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/client-profile").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/staff-profile").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/auth/staff-profile").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/auth/admin-profile").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/admin-profile").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/auth/admin/users").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/register-admin").permitAll()
                        // Assignment endpoints - role-based
                        .requestMatchers(HttpMethod.POST, "/api/assignments").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/assignments").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/assignments/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/assignments/**").authenticated()
                        // Staffing request endpoints - role-based
                        .requestMatchers(HttpMethod.POST, "/api/staffing-requests").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/staffing-requests").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/staffing-requests/**").authenticated()
                        // Attendance endpoints - staff only
                        .requestMatchers(HttpMethod.POST, "/api/attendance/**").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/attendance/**").authenticated()
                        // Rate config - admin only
                        .requestMatchers(HttpMethod.POST, "/api/rates").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/rates").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/rates/**").authenticated()
                        // Invoice endpoints
                        .requestMatchers(HttpMethod.POST, "/api/invoices/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/invoices").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/invoices/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/invoices/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // Payment endpoints
                        .requestMatchers(HttpMethod.POST, "/api/payments/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/payments/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // Billing summary - admin only
                        .requestMatchers(HttpMethod.GET, "/api/billing/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
