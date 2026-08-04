package com.nurseadda.project.security;

import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileVerificationFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StaffProfileRepository staffProfileRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ProfileVerificationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email, String... roles) {
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, authorities)
        );
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    @Test
    @DisplayName("unverified staff: blocked on non-profile endpoints with 403")
    void unverifiedStaff_blockedOnOtherEndpoints() throws Exception {
        authenticateAs("rohan@test.com", "ROLE_STAFF");
        request.setServletPath("/api/auth/staff");

        User user = user(5L, "rohan@test.com");
        StaffProfile profile = new StaffProfile();
        profile.setUser(user);
        profile.setVerified(false);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(Optional.of(profile));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("pending verification");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("unverified staff: profile page and document re-upload are allowed")
    void unverifiedStaff_allowedOnProfileEndpoints() throws Exception {
        authenticateAs("rohan@test.com", "ROLE_STAFF");
        request.setServletPath("/api/auth/staff-profile");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("unverified staff: re-upload endpoint is allowed")
    void unverifiedStaff_allowedOnReuploadEndpoint() throws Exception {
        authenticateAs("rohan@test.com", "ROLE_STAFF");
        request.setServletPath("/api/auth/staff-profile/documents/100");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("verified staff: passes through to any endpoint")
    void verifiedStaff_passes() throws Exception {
        authenticateAs("rohan@test.com", "ROLE_STAFF");
        request.setServletPath("/api/auth/staff");

        User user = user(5L, "rohan@test.com");
        StaffProfile profile = new StaffProfile();
        profile.setUser(user);
        profile.setVerified(true);

        when(userRepository.findByEmail("rohan@test.com")).thenReturn(Optional.of(user));
        when(staffProfileRepository.findByUserId(5L)).thenReturn(Optional.of(profile));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("non-staff roles: pass through without touching the database")
    void nonStaff_passes() throws Exception {
        authenticateAs("root@nurseadda.com", "ROLE_SUPER_ADMIN");
        request.setServletPath("/api/admin/users");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("unauthenticated requests: pass through")
    void unauthenticated_passes() throws Exception {
        request.setServletPath("/api/auth/staff-profile");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }
}
