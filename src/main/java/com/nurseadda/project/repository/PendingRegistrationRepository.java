package com.nurseadda.project.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurseadda.project.model.PendingRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PendingRegistrationRepository {

    private static final String KEY_PREFIX = "pending:registration:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(PendingRegistration pending, long ttlMinutes) {
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + pending.getEmail(),
                    objectMapper.writeValueAsString(pending),
                    Duration.ofMinutes(ttlMinutes)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize pending registration", e);
        }
    }

    public Optional<PendingRegistration> findByEmail(String email) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + email);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, PendingRegistration.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize pending registration", e);
        }
    }

    public void deleteByEmail(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
