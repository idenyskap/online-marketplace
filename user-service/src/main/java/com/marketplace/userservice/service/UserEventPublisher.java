package com.marketplace.userservice.service;

import com.marketplace.userservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void publish(UserEvent event) {
        log.info("Publishing user event: type={} userId={} eventId={}",
                event.getType(), event.getUserId(), event.getEventId());

        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event);
    }
}
