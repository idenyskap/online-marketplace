package com.marketplace.productservice.listener;

import com.marketplace.productservice.event.OrderEvent;
import com.marketplace.productservice.event.OrderItemEvent;
import com.marketplace.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProductService productService;

    @KafkaListener(topics = "order-events", groupId = "product-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received Kafka event: {}", event.getEventType());

        if ("ORDER_FAILED".equals(event.getEventType())) {
            log.info("ORDER FAILED! orderId={}, restoring stock...", event.getOrderId());

            if (event.getItems() != null) {
                for (OrderItemEvent item : event.getItems()) {
                    try {
                        productService.restoreStock(item.getProductId(), item.getQuantity());
                        log.info("Stock restored for product: {}, quantity: {}",
                                item.getProductId(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("Failed to restore stock for product: {}. Error: {}",
                                item.getProductId(), e.getMessage());
                    }
                }
            }
        }
    }
}
