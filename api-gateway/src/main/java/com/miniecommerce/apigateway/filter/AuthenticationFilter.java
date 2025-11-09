package com.miniecommerce.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

   // Public endpoints that don't require authentication
   private static final List<String> PUBLIC_ENDPOINTS = List.of(
           "/gateway/info",
           "/gateway/routes",
           "/actuator",
           "/fallback"
   );

   // API key injected from environment/config (fallback to demo value). Replace by JWT in production.
   private final String validApiKey;

   public AuthenticationFilter(@Value("${API_KEY:example-api-key-12345}") String validApiKey) {
       this.validApiKey = validApiKey;
   }

   @Override
   public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
       ServerHttpRequest request = exchange.getRequest();
       String path = request.getPath().toString();

       // Skip authentication for public endpoints
       if (PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
           return chain.filter(exchange);
       }

       // Check for API key in header
    String apiKey = request.getHeaders().getFirst("X-API-Key");

    if (apiKey == null || !apiKey.equals(validApiKey)) {
           log.warn("Unauthorized access attempt to: {}", path);

           ServerHttpResponse response = exchange.getResponse();
           response.setStatusCode(HttpStatus.UNAUTHORIZED);
           response.getHeaders().add("Content-Type", "application/json");

           String errorResponse = """
               {
                 "timestamp": "%s",
                 "status": 401,
                 "error": "Unauthorized",
                 "message": "Missing or invalid API key. Add 'X-API-Key' header.",
                 "path": "%s"
               }
               """.formatted(java.time.LocalDateTime.now(), path);

           return response.writeWith(Mono.just(
                   response.bufferFactory().wrap(errorResponse.getBytes())
           ));
       }

       log.info("Authenticated request to: {}", path);
       return chain.filter(exchange);
   }

   @Override
   public int getOrder() {
       return -2;  // Execute before logging filter
   }
}