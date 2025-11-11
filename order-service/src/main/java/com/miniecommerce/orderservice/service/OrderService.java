package com.miniecommerce.orderservice.service;

import com.miniecommerce.orderservice.client.ProductClient;
import com.miniecommerce.orderservice.client.UserClient;
import com.miniecommerce.orderservice.dto.OrderRequest;
import com.miniecommerce.orderservice.dto.ProductDTO;
import com.miniecommerce.orderservice.dto.UserDTO;
import com.miniecommerce.orderservice.entity.Order;
import com.miniecommerce.orderservice.event.OrderEvent;
import com.miniecommerce.orderservice.exception.BadRequestException;
import com.miniecommerce.orderservice.exception.ResourceNotFoundException;
import com.miniecommerce.orderservice.exception.ServiceCommunicationException;
import com.miniecommerce.orderservice.kafka.OrderEventProducer;
import com.miniecommerce.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final OrderEventProducer orderEventProducer;

    @RateLimiter(name = "orderService")
    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Transactional
    @CircuitBreaker(name = "orderService")
    @RateLimiter(name = "orderService")
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for user: {} and product: {}",
                request.getUserId(), request.getProductId());

        // 1. Fetch user details with circuit breaker
        UserDTO user;
        try {
            user = userClient.getUserById(request.getUserId());
            log.info("User found: {}", user.getName());

            // Check if we got fallback data
            if ("Service Unavailable".equals(user.getName())) {
                throw new ServiceCommunicationException("User Service",
                        "User service is currently unavailable");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", request.getUserId());
        } catch (Exception e) {
            log.error("Error fetching user: {}", e.getMessage());
            throw new ServiceCommunicationException("User Service", e.getMessage());
        }

        // 2. Fetch product details with circuit breaker
        ProductDTO product;
        try {
            product = productClient.getProductById(request.getProductId());
            log.info("Product found: {}", product.getName());

            // Check if we got fallback data
            if ("Product Unavailable".equals(product.getName())) {
                throw new ServiceCommunicationException("Product Service",
                        "Product service is currently unavailable");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product", "id", request.getProductId());
        } catch (Exception e) {
            log.error("Error fetching product: {}", e.getMessage());
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

        // PUBLISH ORDER_CREATED EVENT
        orderEventProducer.sendOrderEvent(
                OrderEvent.createOrderCreatedEvent(
                        savedOrder.getId(),
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        product.getId(),
                        product.getName(),
                        request.getQuantity(),
                        totalPrice
                )
        );

        // 6. Update stock with circuit breaker
        try {
            productClient.updateStock(product.getId(), -request.getQuantity());
            savedOrder.setStatus("COMPLETED");
            orderRepository.save(savedOrder);
            log.info("Product stock updated successfully");

            // PUBLISH ORDER_COMPLETED EVENT
            orderEventProducer.sendOrderEvent(
                    OrderEvent.createOrderCompletedEvent(
                            savedOrder.getId(),
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            product.getId(),
                            product.getName(),
                            request.getQuantity(),
                            totalPrice
                    )
            );
        } catch (Exception e) {
            log.error("Failed to update product stock: {}", e.getMessage());
            savedOrder.setStatus("FAILED");
            orderRepository.save(savedOrder);

            // PUBLISH ORDER_FAILED EVENT
            orderEventProducer.sendOrderEvent(
                    OrderEvent.createOrderFailedEvent(savedOrder.getId(), e.getMessage())
            );

            throw new ServiceCommunicationException("Product Service",
                    "Failed to update stock. Order marked as FAILED: " + e.getMessage());
        }

        return savedOrder;
    }

    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public List<Order> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user: {}", userId);

        try {
            UserDTO user = userClient.getUserById(userId);
            if ("Service Unavailable".equals(user.getName())) {
                log.warn("User service unavailable, returning orders without validation");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("User", "id", userId);
        } catch (Exception e) {
            log.warn("Could not validate user existence, proceeding anyway: {}", e.getMessage());
        }

        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByProductId(Long productId) {
        log.info("Fetching orders for product: {}", productId);
        return orderRepository.findByProductId(productId);
    }

    public Order updateOrderStatus(Long id, String status) {
        log.info("Updating order status for id: {} to: {}", id, status);
        Order order = getOrderById(id);
        order.setStatus(status);
        Order updated = orderRepository.save(order);
        log.info("Order status updated successfully");
        return updated;
    }

    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);
        Order order = getOrderById(id);
        orderRepository.delete(order);
        log.info("Order deleted successfully");
    }
}