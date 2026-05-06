package com.marketplace.orderservice.controller;

import com.marketplace.orderservice.service.PaymentEventHandler;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentEventHandler paymentEventHandler;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("Received Stripe event: type={}, id={}", event.getType(), event.getId());

        try {
            paymentEventHandler.handle(event);
        } catch (Exception e) {
            log.error("Failed to process event {}: {}", event.getId(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Processing error");
        }

        return ResponseEntity.ok("Received");
    }
}
