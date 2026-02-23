package com.marketplace.orderservice.repository;

import com.marketplace.orderservice.entity.Order;
import com.marketplace.orderservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status);
}
