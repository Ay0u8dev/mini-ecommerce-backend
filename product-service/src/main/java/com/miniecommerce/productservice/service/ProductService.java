package com.miniecommerce.productservice.service;

import com.miniecommerce.productservice.entity.Product;
import com.miniecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Create new product
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // Update product
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());
        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    // Get products by category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // Search products by name
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    // Get available products (in stock)
    public List<Product> getAvailableProducts() {
        return productRepository.findByStockGreaterThan(0);
    }

    // Update stock (useful for order processing)
    public Product updateStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(newStock);
        return productRepository.save(product);
    }
}
