package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.sagaorchestrator.client.AccountServiceFeignClient;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaResponse;
import com.rajeswaran.sagaorchestrator.dto.AccountCreateRequest;
import com.rajeswaran.common.events.AccountOpenedEvent;
import com.rajeswaran.common.events.AccountOpeningFailedEvent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.Instant;

@Service
public class AccountOpeningSagaService {
    private final WebClient webClient;
    private final StreamBridge streamBridge;
    private final AccountServiceFeignClient accountServiceFeignClient;

    public AccountOpeningSagaService(WebClient.Builder webClientBuilder, StreamBridge streamBridge, AccountServiceFeignClient accountServiceFeignClient) {
        this.webClient = webClientBuilder.build();
        this.streamBridge = streamBridge;
        this.accountServiceFeignClient = accountServiceFeignClient;
    }

    // URLs for user-service and account-service via API Gateway
    private static final String USER_SERVICE_URL = "http://localhost:8080/api/users";
    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8080/api/accounts";

    public AccountOpeningSagaResponse executeAccountOpeningSaga(AccountOpeningSagaRequest request, String authorizationHeader) {
        String userId = null;
        String accountId = null;
        try {
            // 1. Create user in user-service
            var userPayload = new UserCreateRequest(request.username(), request.email(), request.fullName());
            var userResponse = webClient.post()
                    .uri(USER_SERVICE_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .headers(headers -> {
                        if (authorizationHeader != null) {
                            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                        }
                    })
                    .bodyValue(userPayload)
                    .retrieve()
                    .bodyToMono(UserCreateResponse.class)
                    .block();
            if (userResponse == null || userResponse.id() == null) {
                var event = new AccountOpeningFailedEvent(null, null, request.email(), request.fullName(), Instant.now(), "User creation failed", "User creation failed");
                streamBridge.send("auditEvent-out-0", event);
                streamBridge.send("notificationEvent-out-0", event);
                return new AccountOpeningSagaResponse("FAILED", "User creation failed", null, null);
            }
            userId = userResponse.id();

            // 2. Create account in account-service (using Feign)
            var accountPayload = new AccountCreateRequest(null, null, request.accountType(), userId, 0.0, "ACTIVE");
            var accountResponse = accountServiceFeignClient.createAccount(accountPayload, authorizationHeader);
            if (accountResponse == null || accountResponse.accountId() == null) {
                var event = new AccountOpeningFailedEvent(userId, null, request.email(), request.fullName(), Instant.now(), "Account creation failed", "Account creation failed, user will be deleted (compensation not implemented)");
                streamBridge.send("auditEvent-out-0", event);
                streamBridge.send("notificationEvent-out-0", event);
                return new AccountOpeningSagaResponse("FAILED", "Account creation failed, user will be deleted (compensation not implemented)", userId, null);
            }
            accountId = accountResponse.accountId();

            // Success: publish AccountOpenedEvent
            var openedEvent = new AccountOpenedEvent(userId, accountId, request.email(), request.fullName(), Instant.now(), "User and account created successfully");
            streamBridge.send("auditEvent-out-0", openedEvent);
            streamBridge.send("notificationEvent-out-0", openedEvent);
            return new AccountOpeningSagaResponse("SUCCESS", "User and account created successfully", userId, accountId);
        } catch (Exception ex) {
            var event = new AccountOpeningFailedEvent(userId, accountId, request.email(), request.fullName(), Instant.now(), "Saga exception", "Saga failed: " + ex.getMessage());
            streamBridge.send("auditEvent-out-0", event);
            streamBridge.send("notificationEvent-out-0", event);
            return new AccountOpeningSagaResponse("FAILED", "Saga failed: " + ex.getMessage(), userId, accountId);
        }
    }

    // DTOs for user-service and account-service requests/responses
    private record UserCreateRequest(String username, String email, String fullName) {}
    private record UserCreateResponse(String id, String username, String email, String fullName) {}
}
