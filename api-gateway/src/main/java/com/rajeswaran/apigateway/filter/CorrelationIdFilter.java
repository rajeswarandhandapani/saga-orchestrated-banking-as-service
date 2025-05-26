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

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            request = exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build();
            exchange = exchange.mutate().request(request).build();
        }
        MDC.put(CORRELATION_ID_HEADER, correlationId);
        return chain.filter(exchange).doFinally(signalType -> MDC.remove(CORRELATION_ID_HEADER));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

