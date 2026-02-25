package com.marketplace.notificationservice.listener;

import com.marketplace.notificationservice.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received Kafka event: {}", event.getEventType());

        switch (event.getEventType()) {
            case "ORDER_CREATED" -> {
                log.info("NEW ORDER! orderId={}, buyerId={}, total={}, items={}",
                        event.getOrderId(),
                        event.getBuyerId(),
                        event.getTotalAmount(),
                        event.getItemCount());
                log.info("Email sent to buyer {}: Your order #{} has been placed!",
                        event.getBuyerId(), event.getOrderId());
            }

            case "ORDER_STATUS_CHANGED" -> {
                log.info("ORDER STATUS CHANGED! orderId={}, newStatus={}",
                        event.getOrderId(),
                        event.getStatus());
                log.info("Email sent to buyer {}: Your order #{} is now {}",
                        event.getBuyerId(), event.getOrderId(), event.getStatus());
            }

            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
