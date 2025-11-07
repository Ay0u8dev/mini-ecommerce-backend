package com.miniecommerce.orderservice.client;

import com.miniecommerce.orderservice.dto.ProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productService")
    ProductDTO getProductById(@PathVariable Long id);

    @PutMapping("/products/{id}/stock")
    @CircuitBreaker(name = "productService", fallbackMethod = "updateStockFallback")
    @Retry(name = "productService")
    ProductDTO updateStock(@PathVariable Long id, @RequestParam Integer quantity);

    // Fallback methods
    default ProductDTO getProductByIdFallback(Long id, Exception ex) {
        ProductDTO fallbackProduct = new ProductDTO();
        fallbackProduct.setId(id);
        fallbackProduct.setName("Product Service Unavailable");
        fallbackProduct.setDescription("Service is temporarily down");
        fallbackProduct.setPrice(0.0);
        fallbackProduct.setStock(0);
        fallbackProduct.setCategory("N/A");
        return fallbackProduct;
    }

    default ProductDTO updateStockFallback(Long id, Integer quantity, Exception ex) {
        // For stock updates, we should throw an exception rather than silently fail
        throw new RuntimeException("Product service unavailable. Stock update failed.");
    }
}
