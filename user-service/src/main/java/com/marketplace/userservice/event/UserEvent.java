package com.marketplace.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {

    private String eventId;
    private Long userId;
    private UserEventType type;
    private Instant occurredAt;
}
