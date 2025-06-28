package com.rajeswaran.apigateway.handler;

import com.rajeswaran.apigateway.client.AdminDashboardClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AdminCompositeHandler {

    private final AdminDashboardClient adminDashboardClient;

    public Mono<ServerResponse> getAdminDashboard(ServerRequest serverRequest) {

        String authHeader = serverRequest.headers().firstHeader("Authorization");

        Mono<ResponseEntity<Object>> users = adminDashboardClient.fetchUsers(authHeader);
        Mono<ResponseEntity<Object>> accounts = adminDashboardClient.fetchAccounts(authHeader);
        Mono<ResponseEntity<Object>> transactions = adminDashboardClient.fetchTransactions(authHeader);
        Mono<ResponseEntity<Object>> payments = adminDashboardClient.fetchPayments(authHeader);
        Mono<ResponseEntity<Object>> auditLogs = adminDashboardClient.fetchAuditLogs(authHeader);
        Mono<ResponseEntity<Object>> notifications = adminDashboardClient.fetchNotifications(authHeader);

        return Mono.zip(users, accounts, transactions, payments, auditLogs, notifications)
                .flatMap(tuple -> {
                    Object usersObj = tuple.getT1().getBody();
                    Object accountsObj = tuple.getT2().getBody();
                    Object transactionsObj = tuple.getT3().getBody();
                    Object paymentsObj = tuple.getT4().getBody();
                    Object auditLogsObj = tuple.getT5().getBody();
                    Object notificationsObj = tuple.getT6().getBody();

                    return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .bodyValue(
                            new Object[] { usersObj, accountsObj, transactionsObj, paymentsObj, auditLogsObj, notificationsObj }
                    );
                });
    }

}
