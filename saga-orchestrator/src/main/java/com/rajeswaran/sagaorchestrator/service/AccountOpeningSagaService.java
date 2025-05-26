package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.common.events.SagaEvent;
import com.rajeswaran.sagaorchestrator.client.AccountServiceFeignClient;
import com.rajeswaran.sagaorchestrator.client.UserServiceFeignClient;
import com.rajeswaran.sagaorchestrator.dto.AccountCreateRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaResponse;
import com.rajeswaran.sagaorchestrator.dto.UserCreateRequest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Service
public class AccountOpeningSagaService {
    private final WebClient webClient;
    private final StreamBridge streamBridge;
    private final AccountServiceFeignClient accountServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;

    public AccountOpeningSagaService(WebClient.Builder webClientBuilder, StreamBridge streamBridge, AccountServiceFeignClient accountServiceFeignClient, UserServiceFeignClient userServiceFeignClient) {
        this.webClient = webClientBuilder.build();
        this.streamBridge = streamBridge;
        this.accountServiceFeignClient = accountServiceFeignClient;
        this.userServiceFeignClient = userServiceFeignClient;
    }

    // URLs for user-service and account-service via API Gateway
    private static final String USER_SERVICE_URL = "http://localhost:8080/api/users";
    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8080/api/accounts";

    public AccountOpeningSagaResponse executeAccountOpeningSaga(AccountOpeningSagaRequest request, String authorizationHeader, String correlationIdHeader) {
        String userId = null;
        String accountId = null;
        var correlationId = new SagaEvent.CorrelationId(correlationIdHeader);
        try {
            // 1. Create user in user-service (using Feign)
            var userPayload = new UserCreateRequest(request.username(), request.email(), request.fullName());
            var userResponse = userServiceFeignClient.createUser(userPayload, authorizationHeader);
            if (userResponse == null || userResponse.id() == null) {
                var event = new SagaEvent(
                    null,
                    null,
                    Instant.now(),
                    "User creation failed",
                    correlationId,
                    SagaEvent.ServiceName.SAGA_ORCHESTRATOR,
                    SagaEvent.SagaEventType.ACCOUNT_OPEN_FAILED
                );
                streamBridge.send("auditEvent-out-0", event);
                streamBridge.send("notificationEvent-out-0", event);
                return new AccountOpeningSagaResponse("FAILED", "User creation failed", null, null);
            }
            userId = userResponse.id();

            // 2. Create account in account-service (using Feign)
            var accountPayload = new AccountCreateRequest(null, null, request.accountType(), userId, 0.0, "ACTIVE");
            var accountResponse = accountServiceFeignClient.createAccount(accountPayload, authorizationHeader);
            if (accountResponse == null || accountResponse.accountId() == null) {
                var event = new SagaEvent(
                    userId,
                    null,
                    Instant.now(),
                    "Account creation failed",
                    correlationId,
                    SagaEvent.ServiceName.SAGA_ORCHESTRATOR,
                    SagaEvent.SagaEventType.ACCOUNT_OPEN_FAILED
                );
                streamBridge.send("auditEvent-out-0", event);
                streamBridge.send("notificationEvent-out-0", event);
                return new AccountOpeningSagaResponse("FAILED", "Account creation failed, user will be deleted (compensation not implemented)", userId, null);
            }
            accountId = accountResponse.accountId();

            // Success: publish SagaEvent for ACCOUNT_OPENED
            var openedEvent = new SagaEvent(
                userId,
                accountId,
                Instant.now(),
                "User and account created successfully",
                correlationId,
                SagaEvent.ServiceName.SAGA_ORCHESTRATOR,
                SagaEvent.SagaEventType.ACCOUNT_OPENED
            );
            streamBridge.send("auditEvent-out-0", openedEvent);
            streamBridge.send("notificationEvent-out-0", openedEvent);
            return new AccountOpeningSagaResponse("SUCCESS", "User and account created successfully", userId, accountId);
        } catch (Exception ex) {
            var event = new SagaEvent(
                userId,
                accountId,
                Instant.now(),
                "Saga failed: " + ex.getMessage(),
                correlationId,
                SagaEvent.ServiceName.SAGA_ORCHESTRATOR,
                SagaEvent.SagaEventType.ACCOUNT_OPEN_FAILED
            );
            streamBridge.send("auditEvent-out-0", event);
            streamBridge.send("notificationEvent-out-0", event);
            return new AccountOpeningSagaResponse("FAILED", "Saga failed: " + ex.getMessage(), userId, accountId);
        }
    }
}
