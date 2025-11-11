package com.miniecommerce.productservice.service;

import com.miniecommerce.productservice.entity.Product;
import com.miniecommerce.productservice.event.ProductEvent;
import com.miniecommerce.productservice.exception.InsufficientStockException;
import com.miniecommerce.productservice.exception.ResourceNotFoundException;
import com.miniecommerce.productservice.kafka.ProductEventProducer;
import com.miniecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    private static final int LOW_STOCK_THRESHOLD = 10;

    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        // PUBLISH PRODUCT_CREATED EVENT
        productEventProducer.sendProductEvent(
                ProductEvent.createProductCreatedEvent(
                        savedProduct.getId(),
                        savedProduct.getName(),
                        savedProduct.getPrice(),
                        savedProduct.getStock(),
                        savedProduct.getCategory()
                )
        );
        return savedProduct;
    }

    public Product updateProduct(Long id, Product productDetails) {
        log.info("Updating product with id: {}", id);

        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");
        return updatedProduct;
    }

    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        Product product = getProductById(id);
        productRepository.delete(product);
        log.info("Product deleted successfully");
    }

    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        log.info("Searching products with keyword: {}", keyword);
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> getAvailableProducts() {
        log.info("Fetching available products");
        return productRepository.findByStockGreaterThan(0);
    }

    public Product updateStock(Long id, Integer quantity) {
        log.info("Updating stock for product id: {} by quantity: {}", id, quantity);

        Product product = getProductById(id);
        int newStock = product.getStock() + quantity;

        if (newStock < 0) {
            throw new InsufficientStockException(product.getName(), product.getStock(), Math.abs(quantity));
        }

        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated successfully. New stock: {}", updatedProduct.getStock());

        // CHECK AND PUBLISH STOCK EVENTS
        checkAndPublishStockEvents(updatedProduct);

        return updatedProduct;
    }

    private void checkAndPublishStockEvents(Product product) {
        if (product.getStock() == 0) {
            log.warn("⚠️ Product {} is OUT OF STOCK", product.getName());
            productEventProducer.sendProductEvent(
                    ProductEvent.createStockOutEvent(product.getId(), product.getName())
            );
        } else if (product.getStock() > 0 && product.getStock() <= LOW_STOCK_THRESHOLD) {
            log.warn("⚠️ Product {} has LOW STOCK: {}", product.getName(), product.getStock());
            productEventProducer.sendProductEvent(
                    ProductEvent.createStockLowEvent(product.getId(), product.getName(), product.getStock())
            );
        }
    }
}
