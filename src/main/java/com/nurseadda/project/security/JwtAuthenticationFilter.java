package com.nurseadda.project.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = jwtUtil.retrieveTokenFromRequest(request);
        try {
            if (token == null)
                throw new AuthenticationException("Authorization header is empty");

            String email = jwtUtil.retrieveEmailFromToken(token);
            List<String> roles = jwtUtil.retrieveRolesFromToken(token);

            List<GrantedAuthority> authorities =
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .map(authority ->
                                    (GrantedAuthority) authority)
                            .toList();

            /*
                Create Authentication Object
             */

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            log.info("Authentication successful");
            log.info("Email : " + email);
            log.info("Roles : " + roles);

        }
        catch (AuthenticationException e) {
            log.error("Authentication failed : {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                        {
                            "message":"Authorization header is empty"
                        }
                    """);
            return;
        }
        catch (JWTVerificationException e) {
            log.error("Invalid JWT token : {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            /*
            response.getWriter().write(
    "{\n" +
    "   \"message\":\"Authorization header is missing\"\n" +
    "}"
);
Java 15 introduced multiline string literals
             */

            response.getWriter().write("""
                        {
                            "message":"Invalid Authorization token"
                        }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/register-staff")
                || path.equals("/api/auth/register-client")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/send-otp")
                || path.equals("/api/auth/verify-otp")
                || path.startsWith("/uploads/");
    }

}
