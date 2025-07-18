package com.rajeswaran.apigateway.handler;

import com.rajeswaran.apigateway.client.AdminDashboardClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCompositeHandler {

    private final AdminDashboardClient adminDashboardClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Mono<ServerResponse> getAdminDashboard(ServerRequest serverRequest) {

        String authHeader = serverRequest.headers().firstHeader("Authorization");

        Mono<ResponseEntity<Object>> users = withCircuitBreaker(
            "usersService",
            () -> adminDashboardClient.fetchUsers(authHeader),
            t -> clientFallback(authHeader, t)
        );
        Mono<ResponseEntity<Object>> accounts = withCircuitBreaker(
            "accountsService",
            () -> adminDashboardClient.fetchAccounts(authHeader),
            t -> clientFallback(authHeader, t)
        );
        Mono<ResponseEntity<Object>> transactions = withCircuitBreaker(
            "transactionsService",
            () -> adminDashboardClient.fetchTransactions(authHeader),
            t -> clientFallback(authHeader, t)
        );
        Mono<ResponseEntity<Object>> payments = withCircuitBreaker(
            "paymentsService",
            () -> adminDashboardClient.fetchPayments(authHeader),
            t -> clientFallback(authHeader, t)
        );
        Mono<ResponseEntity<Object>> notifications = withCircuitBreaker(
            "notificationsService",
            () -> adminDashboardClient.fetchNotifications(authHeader),
            t -> clientFallback(authHeader, t)
        );
        Mono<ResponseEntity<Object>> sagaInstances = withCircuitBreaker(
            "sagaInstancesService",
            () -> adminDashboardClient.fetchSagaInstances(authHeader),
            t -> clientFallback(authHeader, t)
        );

        return Mono.zip(users, accounts, transactions, payments, notifications, sagaInstances)
                .flatMap(tuple -> {
                    Object usersObj = tuple.getT1().getBody();
                    Object accountsObj = tuple.getT2().getBody();
                    Object transactionsObj = tuple.getT3().getBody();
                    Object paymentsObj = tuple.getT4().getBody();
                    Object notificationsObj = tuple.getT5().getBody();
                    Object sagaInstancesObj = tuple.getT6().getBody();

                    return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .bodyValue(
                            Map.of(
                                "users", usersObj,
                                "accounts", accountsObj,
                                "transactions", transactionsObj,
                                "payments", paymentsObj,
                                "notifications", notificationsObj,
                                "sagaInstances", sagaInstancesObj
                            )
                    );
                });
    }

    // Generic circuit breaker wrapper for all fetch calls
    public <T> Mono<T> withCircuitBreaker(String serviceName, Supplier<Mono<T>> supplier, Function<Throwable, Mono<T>> fallback) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        return Mono.defer(supplier)
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .onErrorResume(fallback);
    }

    public Mono<ResponseEntity<Object>> clientFallback(String authHeader, Throwable t) {
        log.warn("FALLBACK TO DASHBOARD CLIENT {}", t.getMessage());
        return Mono.just(ResponseEntity.ok(Collections.emptyList()));
    }

}
