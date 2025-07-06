package com.rajeswaran.apigateway.handler;

import com.rajeswaran.apigateway.client.AdminDashboardClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

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
        Mono<ResponseEntity<Object>> notifications = adminDashboardClient.fetchNotifications(authHeader);
        Mono<ResponseEntity<Object>> sagaInstances = adminDashboardClient.fetchSagaInstances(authHeader);

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

}
