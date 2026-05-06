package com.marketplace.orderservice.service;

import com.marketplace.orderservice.client.ProductClient;
import com.marketplace.orderservice.dto.*;
import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.entity.OrderItem;
import com.marketplace.orderservice.enums.OrderStatus;
import com.marketplace.orderservice.exception.ResourceNotFoundException;
import com.marketplace.orderservice.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProductClient productClient;
    private final OrderPostProcessor orderPostProcessor;
    private final StripeService stripeService;

    @Transactional(rollbackFor = StripeException.class)
    public CheckoutResponse createOrder(OrderRequest request, Long buyerId) throws StripeException {
        log.info("Creating order for buyer: {}", buyerId);

        Order order = Order.builder()
                .buyerId(buyerId)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductResponse product = productClient.getProduct(itemRequest.getProductId());

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(orderItem);

            total = total.add(orderItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        log.info("Order created with id: {}", saved.getId());

        Session session = stripeService.createCheckoutSession(saved);
        saved.setPaymentSessionId(session.getId());

        return CheckoutResponse.builder()
                .orderId(saved.getId())
                .status(saved.getStatus())
                .totalAmount(saved.getTotalAmount())
                .paymentSessionId(session.getId())
                .checkoutUrl(session.getUrl())
                .build();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        log.info("Updating order {} status: {} → {}", orderId, order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        orderPostProcessor.processStatusChange(updated, "ORDER_STATUS_CHANGED");

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long buyerId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (!order.getBuyerId().equals(buyerId)) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByBuyer(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
