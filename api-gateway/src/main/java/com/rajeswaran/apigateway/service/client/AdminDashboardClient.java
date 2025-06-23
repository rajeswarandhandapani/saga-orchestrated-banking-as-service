package com.rajeswaran.apigateway.service.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AdminDashboardClient {
    
    @GetExchange(value = "lb://ACCOUNT-SERVICE/api/accounts", accept = "application/json")
    Mono<ResponseEntity<List<Object>>> fetchAllAccounts();
    
    @GetExchange(value = "lb://PAYMENT-SERVICE/api/payments", accept = "application/json")
    Mono<ResponseEntity<List<Object>>> fetchAllPayments();
    
    @GetExchange(value = "lb://TRANSACTION-SERVICE/api/transactions", accept = "application/json")
    Mono<ResponseEntity<List<Object>>> fetchAllTransactions();
    
    @GetExchange(value = "lb://AUDIT-SERVICE/api/audit-logs", accept = "application/json")
    Mono<ResponseEntity<List<Object>>> fetchAllAuditLogs();
    
    @GetExchange(value = "lb://NOTIFICATION-SERVICE/api/notifications", accept = "application/json")
    Mono<ResponseEntity<List<Object>>> fetchAllNotifications();
    
    @GetExchange(value = "lb://USER-SERVICE/api/users", accept = "application/json")
    Mono<ResponseEntity<List<Object>>> fetchAllUsers();
}
