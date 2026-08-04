package com.nurseadda.project.security;

import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.repository.StaffProfileRepository;
import com.nurseadda.project.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Blocks STAFF users whose profile has not been verified yet by an
 * administrator. Unverified staff members may only access their own profile
 * page (view / update) and the uploaded files; everything else returns 403.
 */
@Component
@RequiredArgsConstructor
public class ProfileVerificationFilter extends OncePerRequestFilter {

    private static final String ROLE_STAFF = "ROLE_STAFF";

    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String email)
                || !isStaff(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAllowedForUnverifiedStaff(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean verified = userRepository.findByEmail(email)
                .map(User::getId)
                .flatMap(staffProfileRepository::findByUserId)
                .map(StaffProfile::isVerified)
                .orElse(false);

        if (verified) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                    "message": "Your profile is pending verification. You can only update your profile until it is verified by an administrator."
                }
                """);
    }

    private boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_STAFF.equals(authority.getAuthority()));
    }

    private boolean isAllowedForUnverifiedStaff(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/staff-profile")
                || path.startsWith("/api/auth/staff-profile/")
                || path.startsWith("/uploads/");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Public endpoints that are already permitAll (matches the JWT filter skip list)
        String path = request.getServletPath();
        return path.equals("/api/auth/register-staff")
                || path.equals("/api/auth/register-client")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/send-otp")
                || path.equals("/api/auth/verify-otp");
    }
}
