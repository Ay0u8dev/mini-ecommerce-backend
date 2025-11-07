package com.miniecommerce.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("========== Gateway Request ==========");
        log.info("Request Time: {}", LocalDateTime.now());
        log.info("Request Method: {}", request.getMethod());
        log.info("Request Path: {}", request.getPath());
        log.info("Request URI: {}", request.getURI());
        log.info("Remote Address: {}", request.getRemoteAddress());
        log.info("Headers: {}", request.getHeaders());
        log.info("====================================");

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.info("========== Gateway Response ==========");
            log.info("Response Status: {}", exchange.getResponse().getStatusCode());
            log.info("======================================");
        }));
    }

    @Override
    public int getOrder() {
        return -1;  // High priority - execute first
    }
}