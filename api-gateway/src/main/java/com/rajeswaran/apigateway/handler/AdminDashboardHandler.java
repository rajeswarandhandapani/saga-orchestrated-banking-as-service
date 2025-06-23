package com.rajeswaran.apigateway.handler;

import com.rajeswaran.apigateway.dto.AdminDashboardDto;
import com.rajeswaran.apigateway.service.client.AdminDashboardClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDashboardHandler {

    private final AdminDashboardClient adminDashboardClient;

    public Mono<ServerResponse> fetchAdminDashboard(ServerRequest serverRequest) {
        log.info("Received request: fetchAdminDashboard");

        // Make parallel calls to all microservices with error handling
        Mono<List<Object>> accountsResponse = adminDashboardClient.fetchAllAccounts()
                .map(ResponseEntity::getBody)
                .onErrorReturn(List.of())
                .doOnError(throwable -> log.warn("Failed to fetch accounts: {}", throwable.getMessage()));
        
        Mono<List<Object>> paymentsResponse = adminDashboardClient.fetchAllPayments()
                .map(ResponseEntity::getBody)
                .onErrorReturn(List.of())
                .doOnError(throwable -> log.warn("Failed to fetch payments: {}", throwable.getMessage()));
        
        Mono<List<Object>> transactionsResponse = adminDashboardClient.fetchAllTransactions()
                .map(ResponseEntity::getBody)
                .onErrorReturn(List.of())
                .doOnError(throwable -> log.warn("Failed to fetch transactions: {}", throwable.getMessage()));
        
        Mono<List<Object>> auditLogsResponse = adminDashboardClient.fetchAllAuditLogs()
                .map(ResponseEntity::getBody)
                .onErrorReturn(List.of())
                .doOnError(throwable -> log.warn("Failed to fetch audit logs: {}", throwable.getMessage()));
        
        Mono<List<Object>> notificationsResponse = adminDashboardClient.fetchAllNotifications()
                .map(ResponseEntity::getBody)
                .onErrorReturn(List.of())
                .doOnError(throwable -> log.warn("Failed to fetch notifications: {}", throwable.getMessage()));
        
        Mono<List<Object>> usersResponse = adminDashboardClient.fetchAllUsers()
                .map(ResponseEntity::getBody)
                .onErrorReturn(List.of())
                .doOnError(throwable -> log.warn("Failed to fetch users: {}", throwable.getMessage()));

        // Aggregate all responses using Mono.zip
        return Mono.zip(accountsResponse, paymentsResponse, transactionsResponse, 
                       auditLogsResponse, notificationsResponse, usersResponse)
                .flatMap(tuple -> {
                    List<Object> accounts = tuple.getT1();
                    List<Object> payments = tuple.getT2();
                    List<Object> transactions = tuple.getT3();
                    List<Object> auditLogs = tuple.getT4();
                    List<Object> notifications = tuple.getT5();
                    List<Object> users = tuple.getT6();

                    AdminDashboardDto dashboardData = new AdminDashboardDto(
                            accounts != null ? accounts : List.of(),
                            payments != null ? payments : List.of(),
                            transactions != null ? transactions : List.of(),
                            auditLogs != null ? auditLogs : List.of(),
                            notifications != null ? notifications : List.of(),
                            users != null ? users : List.of()
                    );

                    log.info("Completed request: fetchAdminDashboard - accounts: {}, payments: {}, transactions: {}, auditLogs: {}, notifications: {}, users: {}",
                            accounts != null ? accounts.size() : 0,
                            payments != null ? payments.size() : 0,
                            transactions != null ? transactions.size() : 0,
                            auditLogs != null ? auditLogs.size() : 0,
                            notifications != null ? notifications.size() : 0,
                            users != null ? users.size() : 0);

                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(dashboardData));
                })
                .onErrorResume(throwable -> {
                    log.error("Error occurred while fetching admin dashboard data", throwable);
                    return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue("Error fetching dashboard data"));
                });
    }
}
