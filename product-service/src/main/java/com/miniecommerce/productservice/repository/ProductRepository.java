package com.miniecommerce.productservice.repository;

import com.miniecommerce.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Find products by category
    List<Product> findByCategory(String category);

    // Find products by name containing (case-insensitive search)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find products with stock greater than zero
    List<Product> findByStockGreaterThan(Integer stock);
}
