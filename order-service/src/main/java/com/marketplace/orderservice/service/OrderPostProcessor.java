package com.marketplace.orderservice.service;

import com.marketplace.orderservice.client.ProductClient;
import com.marketplace.orderservice.config.KafkaConfig;
import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.entity.OrderItem;
import com.marketplace.orderservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPostProcessor {

    private final ProductClient productClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Async("orderPostProcessExecutor")
    public void processOrder(Order order, String eventType) {
        log.info("[ASYNC] Starting post-processing for order: {}", order.getId());

        for (OrderItem item : order.getItems()) {
            try {
                productClient.reduceStock(item.getProductId(), item.getQuantity());
                log.info("[ASYNC] Stock reduced for product: {}", item.getProductId());
            } catch (Exception e) {
                log.error("[ASYNC] Failed to reduce stock for product: {}. Error: {}",
                        item.getProductId(), e.getMessage());
            }
        }

        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(order.getId())
                    .buyerId(order.getBuyerId())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .itemCount(order.getItems().size())
                    .eventType(eventType)
                    .build();

            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, order.getId().toString(), event);
            log.info("[ASYNC] Kafka event sent: {} for order: {}", eventType, order.getId());
        } catch (Exception e) {
            log.error("[ASYNC] Failed to send Kafka event for order: {}. Error: {}",
                    order.getId(), e.getMessage());
        }

        log.info("[ASYNC] Post-processing completed for order: {}", order.getId());
    }

    @Async("orderPostProcessExecutor")
    public void processStatusChange(Order order, String eventType) {
        log.info("[ASYNC] Sending status change event for order: {}", order.getId());

        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(order.getId())
                    .buyerId(order.getBuyerId())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .itemCount(order.getItems().size())
                    .eventType(eventType)
                    .build();

            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, order.getId().toString(), event);
            log.info("[ASYNC] Kafka event sent: {} for order: {}", eventType, order.getId());
        } catch (Exception e) {
            log.error("[ASYNC] Failed to send Kafka event for order: {}. Error: {}",
                    order.getId(), e.getMessage());
        }
    }
}