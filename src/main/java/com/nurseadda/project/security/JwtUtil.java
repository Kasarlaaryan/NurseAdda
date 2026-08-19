package com.nurseadda.project.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nurseadda.project.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtUtil {

    private static final String ROLE_TAG = "role";
    private static final String ISSUED_DATE_TAG = "issued-date";
    private static final String TOKEN_TYPE_TAG = "type";
    private static final String TOKEN_ISSUER = "NurseAdda";

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.validity.accessToken}")
    private Long ACCESS_TOKEN_VALIDITY_DURATION;

    @Value("${jwt.validity.refreshToken}")
    private Long REFRESH_TOKEN_VALIDITY_DURATION;

    @Value("${jwt.secret}")
    private String SECRET;

    public String generateAccessToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8));
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_DURATION))
                .withIssuer(TOKEN_ISSUER)
                .withClaim(TOKEN_TYPE_TAG, TYPE_ACCESS)
                .withClaim(ROLE_TAG, user.getAuthorities().stream().map(Object::toString).toList())
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8));
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_DURATION))
                .withIssuer(TOKEN_ISSUER)
                .withClaim(TOKEN_TYPE_TAG, TYPE_REFRESH)
                .withClaim(ISSUED_DATE_TAG, new Date())
                .withClaim(ROLE_TAG, user.getAuthorities().stream().map(Object::toString).toList())
                .sign(algorithm);
    }

    public String retrieveTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private DecodedJWT getDecodedToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8));
        return JWT.require(algorithm).build().verify(token);
    }

    public String retrieveEmailFromToken(String token) throws JWTVerificationException {
        log.info("Decoded JWT token: {}", getDecodedToken(token));
        return getDecodedToken(token).getSubject();
    }

    public List<String> retrieveRolesFromToken(String token) throws JWTVerificationException {
        return getDecodedToken(token).getClaim(ROLE_TAG).asList(String.class);
    }

    public String retrieveTokenType(String token) throws JWTVerificationException {
        return getDecodedToken(token).getClaim(TOKEN_TYPE_TAG).asString();
    }

    /**
     * Remaining lifetime of the token in seconds, used to bound how long a
     * revoked token stays blacklisted. Returns 0 for expired/invalid tokens.
     */
    public long retrieveRemainingValiditySeconds(String token) {
        try {
            long remainingMillis =
                    getDecodedToken(token).getExpiresAt().getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMillis / 1000);
        } catch (JWTVerificationException e) {
            return 0;
        }
    }

}
