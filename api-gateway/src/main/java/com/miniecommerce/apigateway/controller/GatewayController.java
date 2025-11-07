package com.miniecommerce.apigateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class GatewayController {

    private final RouteLocator routeLocator;

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> routes = new ArrayList<>();

        routeLocator.getRoutes().subscribe(route -> {
            Map<String, String> routeInfo = new HashMap<>();
            routeInfo.put("id", route.getId());
            routeInfo.put("uri", route.getUri().toString());
            routeInfo.put("predicates", route.getPredicate().toString());
            routes.add(routeInfo);
        });

        response.put("totalRoutes", routes.size());
        response.put("routes", routes);
        response.put("gatewayInfo", Map.of(
                "name", "E-Commerce API Gateway",
                "version", "1.0",
                "port", "8080"
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "E-Commerce API Gateway");
        info.put("version", "1.0.0");
        info.put("description", "Centralized API Gateway for E-Commerce Microservices");
        info.put("endpoints", Map.of(
                "users", "/api/users",
                "products", "/api/products",
                "orders", "/api/orders"
        ));
        info.put("features", List.of(
                "Service Discovery",
                "Load Balancing",
                "Circuit Breaker",
                "Rate Limiting",
                "Automatic Retry",
                "CORS Support"
        ));

        return ResponseEntity.ok(info);
    }
}