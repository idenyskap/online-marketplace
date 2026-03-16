package com.marketplace.orderservice.service;

import com.marketplace.orderservice.client.ProductClient;
import com.marketplace.orderservice.dto.OrderItemRequest;
import com.marketplace.orderservice.dto.OrderRequest;
import com.marketplace.orderservice.dto.OrderResponse;
import com.marketplace.orderservice.dto.ProductResponse;
import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.entity.OrderItem;
import com.marketplace.orderservice.enums.OrderStatus;
import com.marketplace.orderservice.exception.ResourceNotFoundException;
import com.marketplace.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderPostProcessor orderPostProcessor;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId("prod1")
                .quantity(2)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        ProductResponse productResponse = ProductResponse.builder()
                .id("prod1")
                .name("iPhone 15")
                .price(BigDecimal.valueOf(999.99))
                .stock(10)
                .build();

        when(productClient.getProduct("prod1")).thenReturn(productResponse);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderResponse result = orderService.createOrder(request, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getBuyerId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(BigDecimal.valueOf(1999.98), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals("iPhone 15", result.getItems().get(0).getProductName());

        verify(productClient).getProduct("prod1");
        verify(orderPostProcessor).processOrder(any(Order.class), any(String.class));
    }

    @Test
    void shouldThrowExceptionWhenNotEnoughStock() {

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId("prod1")
                .quantity(20)
                .build();

        OrderRequest request = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        ProductResponse productResponse = ProductResponse.builder()
                .id("prod1")
                .name("iPhone 15")
                .price(BigDecimal.valueOf(999.99))
                .stock(5)
                .build();

        when(productClient.getProduct("prod1")).thenReturn(productResponse);

        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(request, 1L));
    }

    @Test
    void shouldGetOrderById() {

        Order order = Order.builder()
                .id(1L)
                .buyerId(1L)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(999.99))
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .productId("prod1")
                .productName("iPhone 15")
                .quantity(1)
                .price(BigDecimal.valueOf(999.99))
                .build();
        order.getItems().add(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getBuyerId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(BigDecimal.valueOf(999.99), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById(1L, 1L));
    }

    @Test
    void shouldUpdateOrderStatus() {

        Order order = Order.builder()
                .id(1L)
                .buyerId(1L)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(999.99))
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        OrderResponse result = orderService.updateStatus(1L, OrderStatus.PAID);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());

        verify(orderPostProcessor).processStatusChange(any(Order.class), any(String.class));
    }
}
