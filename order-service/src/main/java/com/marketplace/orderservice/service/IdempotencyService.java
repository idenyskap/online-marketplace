package com.marketplace.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.orderservice.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    public OrderResponse getExistingResponse(String idempotencyKey) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + idempotencyKey);

        if (json != null) {
            log.info("Idempotency key found: {}. Returning cached response.", idempotencyKey);
            try {
                return objectMapper.readValue(json, OrderResponse.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize cached response for key: {}", idempotencyKey);
                return null;
            }
        }

        return null;
    }

    public void saveResponse(String idempotencyKey, OrderResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(KEY_PREFIX + idempotencyKey, json, TTL);
            log.info("Idempotency key saved: {}. TTL: {}", idempotencyKey, TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response for key: {}", idempotencyKey);
        }
    }
}
