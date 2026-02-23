package com.marketplace.orderservice.service;

import com.marketplace.orderservice.config.KafkaConfig;
import com.marketplace.orderservice.dto.*;
import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.entity.OrderItem;
import com.marketplace.orderservice.enums.OrderStatus;
import com.marketplace.orderservice.event.OrderEvent;
import com.marketplace.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long buyerId) {
        log.info("Creating order for buyer: {}", buyerId);

        Order order = Order.builder()
                .buyerId(buyerId)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build();

            order.addItem(item);

            total = total.add(itemRequest.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        log.info("Order created with id: {}", saved.getId());

        sendOrderEvent(saved, "ORDER_CREATED");

        return mapToResponse(saved);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        log.info("Updating order {} status: {} → {}", orderId, order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        sendOrderEvent(updated, "ORDER_STATUS_CHANGED");

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByBuyer(Long buyerId) {
        return orderRepository.findByBuyerIdAndStatus(buyerId, null)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void sendOrderEvent(Order order, String eventType) {
        OrderEvent event = OrderEvent.builder()
                .orderId(order.getId())
                .buyerId(order.getBuyerId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .itemCount(order.getItems().size())
                .eventType(eventType)
                .build();

        log.info("Sending Kafka event: {} for order: {}", eventType, order.getId());
        kafkaTemplate.send(KafkaConfig.ORDER_TOPIC, order.getId().toString(), event);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
