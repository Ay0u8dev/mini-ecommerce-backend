package com.miniecommerce.productservice.event;

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
public class ProductEvent implements Serializable {

    private String eventId;
    private String eventType;  // PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED, STOCK_LOW, STOCK_OUT
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime timestamp;

    public static ProductEvent createProductCreatedEvent(Long productId, String productName,
                                                         BigDecimal price, Integer stock, String category) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PRODUCT_CREATED")
                .productId(productId)
                .productName(productName)
                .price(price)
                .stock(stock)
                .category(category)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ProductEvent createStockLowEvent(Long productId, String productName, Integer stock) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("STOCK_LOW")
                .productId(productId)
                .productName(productName)
                .stock(stock)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ProductEvent createStockOutEvent(Long productId, String productName) {
        return ProductEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("STOCK_OUT")
                .productId(productId)
                .productName(productName)
                .stock(0)
                .timestamp(LocalDateTime.now())
                .build();
    }
}