package com.miniecommerce.orderservice.repository;

import com.miniecommerce.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by user
    List<Order> findByUserId(Long userId);

    // Find orders by product
    List<Order> findByProductId(Long productId);

    // Find orders by status
    List<Order> findByStatus(String status);
}
