package com.miniecommerce.orderservice.service;

import com.miniecommerce.orderservice.client.ProductClient;
import com.miniecommerce.orderservice.client.UserClient;
import com.miniecommerce.orderservice.dto.OrderRequest;
import com.miniecommerce.orderservice.dto.ProductDTO;
import com.miniecommerce.orderservice.dto.UserDTO;
import com.miniecommerce.orderservice.entity.Order;
import com.miniecommerce.orderservice.exception.BadRequestException;
import com.miniecommerce.orderservice.exception.ResourceNotFoundException;
import com.miniecommerce.orderservice.exception.ServiceCommunicationException;
import com.miniecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for user: {} and product: {}",
                request.getUserId(), request.getProductId());

        // 1. Fetch user details
        UserDTO user;
        try {
            user = userClient.getUserById(request.getUserId());
            log.info("User found: {}", user.getName());
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", request.getUserId());
        } catch (Exception e) {
            throw new ServiceCommunicationException("User Service", e.getMessage());
        }

        // 2. Fetch product details
        ProductDTO product;
        try {
            product = productClient.getProductById(request.getProductId());
            log.info("Product found: {}", product.getName());
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product", "id", request.getProductId());
        } catch (Exception e) {
            throw new ServiceCommunicationException("Product Service", e.getMessage());
        }

        // 3. Validate stock
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException(
                    String.format("Insufficient stock for '%s'. Available: %d, Requested: %d",
                            product.getName(), product.getStock(), request.getQuantity())
            );
        }

        // 4. Calculate total price
        Double totalPrice = product.getPrice() * request.getQuantity();

        // 5. Create order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setProductId(product.getId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(totalPrice);
        order.setUserName(user.getName());
        order.setProductName(product.getName());
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());

        // 6. Update stock
        try {
            productClient.updateStock(product.getId(), -request.getQuantity());
            savedOrder.setStatus("COMPLETED");
            orderRepository.save(savedOrder);
            log.info("Product stock updated successfully");
        } catch (Exception e) {
            log.error("Failed to update product stock", e);
            savedOrder.setStatus("FAILED");
            orderRepository.save(savedOrder);
            throw new ServiceCommunicationException("Product Service",
                    "Failed to update stock: " + e.getMessage());
        }

        return savedOrder;
    }

    // Get orders by user
    public List<Order> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user: {}", userId);

        // Verify user exists
        try {
            userClient.getUserById(userId);
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", userId);
        } catch (Exception e) {
            throw new ServiceCommunicationException("User Service", e.getMessage());
        }

        return orderRepository.findByUserId(userId);
    }

    // Get orders by product
    public List<Order> getOrdersByProductId(Long productId) {
        return orderRepository.findByProductId(productId);
    }

    // Update order status
    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Delete order
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}