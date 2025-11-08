package com.miniecommerce.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().getHeaders().add("X-Gateway", "E-Commerce-API-Gateway");
        exchange.getResponse().getHeaders().add("X-Gateway-Version", "1.0");
        exchange.getResponse().getHeaders().add("X-Powered-By", "Spring Cloud Gateway");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}