package com.miniecommerce.orderservice.client;

import com.miniecommerce.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDTO getProductById(@PathVariable Long id);

    @PutMapping("/products/{id}/stock")
    ProductDTO updateStock(@PathVariable Long id, @RequestParam Integer quantity);
}
