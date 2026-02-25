package com.marketplace.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private Long buyerId;
    private String status;
    private BigDecimal totalAmount;
    private int itemCount;
    private String eventType;
}
