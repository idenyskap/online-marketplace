package com.marketplace.orderservice.service;

import com.marketplace.orderservice.client.ProductClient;
import com.marketplace.orderservice.config.KafkaConfig;
import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.entity.OrderItem;
import com.marketplace.orderservice.enums.OrderStatus;
import com.marketplace.orderservice.event.OrderEvent;
import com.marketplace.orderservice.event.OrderItemEvent;
import com.marketplace.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPostProcessor {

    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Async("orderPostProcessExecutor")
    public void processOrder(Order order, String eventType) {
        log.info("[ASYNC] Starting post-processing for order: {}", order.getId());

        boolean failed = false;

        for (OrderItem item : order.getItems()) {
            try {
                productClient.reduceStock(item.getProductId(), item.getQuantity());
                log.info("[ASYNC] Stock reduced for product: {}", item.getProductId());
            } catch (Exception e) {
                log.error("[ASYNC] Failed to reduce stock for product: {}. Error: {}",
                        item.getProductId(), e.getMessage());
                failed = true;
                break;
            }
        }

        if (failed) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("[ASYNC] Order {} canceled due to stock reduction failure", order.getId());
            sendOrderEvent(order, "ORDER_FAILED", order.getItems());
        } else {
            sendOrderEvent(order, "ORDER_CREATED", order.getItems());
        }

        log.info("[ASYNC] Post-processing completed for order: {}", order.getId());
    }

    @Async("orderPostProcessExecutor")
    public void processStatusChange(Order order, String eventType) {
        log.info("[ASYNC] Sending status change event for order: {}", order.getId());
        sendOrderEvent(order, eventType, order.getItems());
    }

    public void sendOrderEvent(Order order, String eventType, List<OrderItem> items) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(order.getId())
                    .buyerId(order.getBuyerId())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .itemCount(order.getItems().size())
                    .eventType(eventType)
                    .items(items.stream()
                            .map(item -> new OrderItemEvent(item.getProductId(), item.getQuantity()))
                            .toList())
                    .build();

            kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, order.getId().toString(), event);
            log.info("[ASYNC] Kafka event sent: {} for order: {}", eventType, order.getId());
        } catch (Exception e) {
            log.error("[ASYNC] Failed to send Kafka event for order: {}. Error: {}",
                    order.getId(), e.getMessage());
        }
    }
}
