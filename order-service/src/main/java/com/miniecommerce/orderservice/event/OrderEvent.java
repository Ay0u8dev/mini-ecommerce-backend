package com.miniecommerce.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent implements Serializable {

    private String eventId;
    private String eventType;  // ORDER_CREATED, ORDER_COMPLETED, ORDER_FAILED, ORDER_CANCELLED
    private Long orderId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double totalPrice;
    private String status;
    private LocalDateTime timestamp;

    public static OrderEvent createOrderCreatedEvent(Long orderId, Long userId, String userName, String userEmail,
                                                     Long productId, String productName,
                                                     Integer quantity, Double totalPrice) {
        return OrderEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .orderId(orderId)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .status("PENDING")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static OrderEvent createOrderCompletedEvent(Long orderId, Long userId, String userName, String userEmail,
                                                       Long productId, String productName,
                                                       Integer quantity, Double totalPrice) {
        return OrderEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("ORDER_COMPLETED")
                .orderId(orderId)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .status("COMPLETED")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static OrderEvent createOrderFailedEvent(Long orderId, String reason) {
        return OrderEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("ORDER_FAILED")
                .orderId(orderId)
                .status("FAILED")
                .timestamp(LocalDateTime.now())
                .build();
    }
}