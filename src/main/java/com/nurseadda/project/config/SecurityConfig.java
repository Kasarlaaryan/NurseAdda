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
                                "/uploads/**"
                        ).permitAll()
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
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
