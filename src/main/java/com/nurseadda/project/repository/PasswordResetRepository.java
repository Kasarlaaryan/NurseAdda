package com.nurseadda.project.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurseadda.project.model.PasswordReset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordResetRepository {

    private static final String KEY_PREFIX = "password:reset:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(PasswordReset passwordReset, long ttlMinutes) {
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + passwordReset.getEmail(),
                    objectMapper.writeValueAsString(passwordReset),
                    Duration.ofMinutes(ttlMinutes)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize password reset request", e);
        }
    }

    public Optional<PasswordReset> findByEmail(String email) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + email);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, PasswordReset.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize password reset request", e);
        }
    }

    public void deleteByEmail(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
