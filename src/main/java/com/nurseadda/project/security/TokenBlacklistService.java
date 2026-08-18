package com.nurseadda.project.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklisted:token:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Revokes a token for the given number of seconds. Once revoked, the token
     * is rejected by {@link JwtAuthenticationFilter} until it naturally expires.
     */
    public void blacklist(String token, long ttlSeconds) {
        if (token == null || token.isBlank() || ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(
                KEY_PREFIX + token,
                "1",
                Duration.ofSeconds(ttlSeconds)
        );
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
