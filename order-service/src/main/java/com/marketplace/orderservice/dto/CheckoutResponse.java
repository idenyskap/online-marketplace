package com.marketplace.orderservice.dto;

import com.marketplace.orderservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {

    private Long orderId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentSessionId;
    private String checkoutUrl;
}
