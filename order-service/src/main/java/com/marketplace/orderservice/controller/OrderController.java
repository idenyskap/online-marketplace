package com.marketplace.orderservice.controller;

import com.marketplace.orderservice.dto.OrderRequest;
import com.marketplace.orderservice.dto.OrderResponse;
import com.marketplace.orderservice.enums.OrderStatus;
import com.marketplace.orderservice.service.IdempotencyService;
import com.marketplace.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey != null) {
            OrderResponse cached = idempotencyService.getExistingResponse(idempotencyKey);
            if (cached != null) {
                return ResponseEntity.ok(cached);
            }
        }

        Long buyerId = (Long) authentication.getPrincipal();
        OrderResponse orderResponse = orderService.createOrder(request, buyerId);

        if (idempotencyKey != null) {
            idempotencyService.saveResponse(idempotencyKey, orderResponse);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {

        Long buyerId = (Long) authentication.getPrincipal();
        OrderResponse orderResponse = orderService.getOrderById(id, buyerId);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        Long buyerId = (Long) authentication.getPrincipal();
        List<OrderResponse> orders = orderService.getOrdersByBuyer(buyerId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        OrderResponse orderResponse = orderService.updateStatus(id, status);
        return ResponseEntity.ok(orderResponse);
    }
}
