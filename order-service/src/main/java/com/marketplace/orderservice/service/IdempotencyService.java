package com.marketplace.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.orderservice.dto.CheckoutResponse;
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
    private static final String EVENT_KEY_PREFIX = "stripe-event:";
    private static final Duration EVENT_TTL = Duration.ofDays(7);

    public CheckoutResponse getExistingResponse(String idempotencyKey) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + idempotencyKey);

        if (json != null) {
            log.info("Idempotency key found: {}. Returning cached response.", idempotencyKey);
            try {
                return objectMapper.readValue(json, CheckoutResponse.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize cached response for key: {}", idempotencyKey);
                return null;
            }
        }

        return null;
    }

    public void saveResponse(String idempotencyKey, CheckoutResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(KEY_PREFIX + idempotencyKey, json, TTL);
            log.info("Idempotency key saved: {}. TTL: {}", idempotencyKey, TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response for key: {}", idempotencyKey);
        }
    }

    public boolean isEventAlreadyProcessed(String eventId) {
        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(EVENT_KEY_PREFIX + eventId, "processed", EVENT_TTL);
        return Boolean.FALSE.equals(wasSet);
    }
}
