package com.miniecommerce.notificationservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEvent implements Serializable {
    private String eventId;
    private String eventType;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime timestamp;
}