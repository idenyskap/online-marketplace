package com.marketplace.orderservice.service;

import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.enums.OrderStatus;
import com.marketplace.orderservice.repository.OrderRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final OrderRepository orderRepository;
    private final OrderPostProcessor orderPostProcessor;
    private final IdempotencyService idempotencyService;

    @Transactional
    public void handle(Event event) {
        if (idempotencyService.isEventAlreadyProcessed(event.getId())) {
            log.info("Event {} already processed, skipping", event.getId());
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            default -> log.debug("Ignoring event type: {}", event.getType());
        }
    }

    public void handleCheckoutCompleted(Event event) {
        Session session;
        try {
            session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            log.error("Failed to deserialized Stripe event data: {}", e.getMessage());
            throw new IllegalStateException("Cannot process event " + event.getId(), e);
        }

        String orderIdStr = session.getMetadata().get("orderId");
        if (orderIdStr == null) {
            log.warn("Stripe session {} has no orderId in metadata, skipping", session.getId());
            return;
        }

        Long orderId = Long.parseLong(orderIdStr);
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Order {} not found for Stripe session {}", orderId, session.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.info("Order {} is in status {}, ignoring webhook", orderId, order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentIntentId(session.getPaymentIntent());
        log.info("Order {} marked as PAID, paymentIntent ={}", orderId, session.getPaymentIntent());

        order.getItems().size();
        orderPostProcessor.processOrder(order, "ORDER_PAID");
    }
}
