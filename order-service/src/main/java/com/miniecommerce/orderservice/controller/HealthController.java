package com.miniecommerce.orderservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/circuit-breakers")
    public ResponseEntity<Map<String, Object>> getCircuitBreakersStatus() {
        Map<String, Object> status = new HashMap<>();

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbStatus = new HashMap<>();
            cbStatus.put("state", cb.getState().toString());
            cbStatus.put("failureRate", cb.getMetrics().getFailureRate());
            cbStatus.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbStatus.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
            cbStatus.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());

            status.put(cb.getName(), cbStatus);
        });

        return ResponseEntity.ok(status);
    }

    @GetMapping("/circuit-breakers/{name}")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus(@PathVariable String name) {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);

            Map<String, Object> status = new HashMap<>();
            status.put("name", name);
            status.put("state", cb.getState().toString());
            status.put("failureRate", cb.getMetrics().getFailureRate() + "%");
            status.put("slowCallRate", cb.getMetrics().getSlowCallRate() + "%");
            status.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            status.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
            status.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            status.put("notPermittedCalls", cb.getMetrics().getNumberOfNotPermittedCalls());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}