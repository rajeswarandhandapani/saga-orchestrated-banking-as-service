package com.rajeswaran.apigateway.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface AdminDashboardClient {

    @GetExchange(value = "/api/users", accept = "application/json")
    Mono<ResponseEntity<Object>> fetchUsers(@RequestHeader("Authorization") String authHeader);

    @GetExchange(value = "/api/accounts", accept = "application/json")
    Mono<ResponseEntity<Object>> fetchAccounts(@RequestHeader("Authorization") String authHeader);

    @GetExchange(value = "/api/payments", accept = "application/json")
    Mono<ResponseEntity<Object>> fetchPayments(@RequestHeader("Authorization") String authHeader);

    @GetExchange(value = "/api/transactions", accept = "application/json")
    Mono<ResponseEntity<Object>> fetchTransactions(@RequestHeader("Authorization") String authHeader);

    @GetExchange(value = "/api/notifications", accept = "application/json")
    Mono<ResponseEntity<Object>> fetchNotifications(@RequestHeader("Authorization") String authHeader);

    @GetExchange(value = "/api/saga/instances", accept = "application/json")
    Mono<ResponseEntity<Object>> fetchSagaInstances(@RequestHeader("Authorization") String authHeader);
}
