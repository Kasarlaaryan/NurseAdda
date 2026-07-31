package com.nurseadda.project.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nurseadda.project.common.exception.JwtVerificationException;
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

    //first need to define the role, issueddate, token issuer;
    private static final String ROLE_TAG = "role";
    private static final String ISSUED_DATE = "issued-date";
    private static final String TOKEN_ISSUER = "nurseadda";

    // calling the values of the token refersh token , and acess token and secrect key of the token;
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
                .withClaim(ISSUED_DATE, new Date())
                .withClaim(ROLE_TAG, user.getAuthorities().stream().map(Object::toString).toList())
                .sign(algorithm);

    }

    public String generateRefreshToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8));
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_DURATION))
                .withIssuer(TOKEN_ISSUER)
                .withClaim(ISSUED_DATE, new Date())
                .withClaim(ROLE_TAG, user.getAuthorities().stream().map(Object::toString).toList())
                .sign(algorithm);

    }

    public String retrieveTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            //Bearer token;
            return authorizationHeader.substring(7);
        }
        return null;
    }

    public DecodedJWT getDecodedToken(String token) throws JwtVerificationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8));
            return JWT.require(algorithm).build().verify(token);
        } catch (Exception e) {
            log.error("JWT verification failed: {}", e.getMessage());
            throw new JwtVerificationException("Invalid or expired JWT token", e);
        }
    }

    public String retriveEmailFromToken(String token) throws JwtVerificationException {
        DecodedJWT decodedToken = getDecodedToken(token);
        log.info("Decoded Jwt Token : {}", decodedToken);
        return decodedToken.getSubject();
    }

    public List<String> retriveRolesFromToken(String token) throws JwtVerificationException {
        return getDecodedToken(token).getClaim(ROLE_TAG).asList(String.class);
    }
}
