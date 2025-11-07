package com.miniecommerce.orderservice.service;

import com.miniecommerce.orderservice.dto.ProductDTO;
import com.miniecommerce.orderservice.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FallbackService {

    public UserDTO getUserFallback(Long userId, Exception ex) {
        log.error("User service fallback triggered for userId: {}. Reason: {}",
                userId, ex.getMessage());

        UserDTO fallback = new UserDTO();
        fallback.setId(userId);
        fallback.setName("Service Unavailable");
        fallback.setEmail("service.down@temp.com");
        fallback.setPhone("N/A");

        return fallback;
    }

    public ProductDTO getProductFallback(Long productId, Exception ex) {
        log.error("Product service fallback triggered for productId: {}. Reason: {}",
                productId, ex.getMessage());

        ProductDTO fallback = new ProductDTO();
        fallback.setId(productId);
        fallback.setName("Product Unavailable");
        fallback.setDescription("Service temporarily down");
        fallback.setPrice(0.0);
        fallback.setStock(0);
        fallback.setCategory("N/A");

        return fallback;
    }

    public ProductDTO updateStockFallback(Long productId, Integer quantity, Exception ex) {
        log.error("Product stock update failed for productId: {}. Reason: {}",
                productId, ex.getMessage());

        // For critical operations like stock updates, we throw an exception
        throw new RuntimeException("Cannot update stock: Product service is unavailable");
    }
}