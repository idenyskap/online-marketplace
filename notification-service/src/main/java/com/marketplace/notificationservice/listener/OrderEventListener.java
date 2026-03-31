package com.marketplace.notificationservice.listener;

import com.marketplace.notificationservice.event.OrderEvent;
import com.marketplace.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EmailService emailService;

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

                emailService.sendOrderConfirmation(event);
            }

            case "ORDER_STATUS_CHANGED" -> {
                log.info("ORDER STATUS CHANGED! orderId={}, newStatus={}",
                        event.getOrderId(),
                        event.getStatus());

                emailService.sendStatusUpdate(event);
            }

            case "ORDER_FAILED" -> {
                log.info("ORDER FAILED! orderId={}", event.getOrderId());
                emailService.sendStatusUpdate(event);
            }

            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
