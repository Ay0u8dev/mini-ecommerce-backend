package com.miniecommerce.orderservice.client;

import com.miniecommerce.orderservice.dto.UserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService")
    UserDTO getUserById(@PathVariable Long id);

    // Fallback method - called when circuit is open
    default UserDTO getUserByIdFallback(Long id, Exception ex) {
        UserDTO fallbackUser = new UserDTO();
        fallbackUser.setId(id);
        fallbackUser.setName("User Service Unavailable");
        fallbackUser.setEmail("unavailable@service.down");
        fallbackUser.setPhone("N/A");
        return fallbackUser;
    }
}