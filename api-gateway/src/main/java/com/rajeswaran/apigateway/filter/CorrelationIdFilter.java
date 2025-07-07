package com.rajeswaran.apigateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that ensures every request entering the system has a correlation ID.
 * This is the SINGLE point of correlation ID generation for the entire system.
 * 
 * Responsibilities:
 * - Checks incoming HTTP requests for existing correlation ID
 * - Generates new correlation ID if none exists
 * - Adds correlation ID to request headers for downstream services
 * - Sets correlation ID in MDC for gateway logging
 * 
 * Note: This header name must match AppConstants.CORRELATION_ID_HEADER in common-lib
 * to ensure consistency between HTTP and messaging layers.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    // This value must match AppConstants.CORRELATION_ID_HEADER in common-lib
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        
        boolean generated = false;
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            generated = true;
            request = exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build();
            exchange = exchange.mutate().request(request).build();
        }
        
        // Set in MDC for gateway logging
        MDC.put("correlationId", correlationId);
        
        // Log correlation ID assignment
        if (generated) {
            System.out.println("Generated new correlation ID: " + correlationId + " for path: " + request.getPath());
        } else {
            System.out.println("Using existing correlation ID: " + correlationId + " for path: " + request.getPath());
        }
        
        return chain.filter(exchange).doFinally(signalType -> {
            MDC.remove("correlationId");
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

