package com.marketplace.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private Long buyerId;
    private String status;
    private BigDecimal totalAmount;
    private int itemCount;
    private String eventType;
    private List<OrderItemEvent> items;
}
