package com.rajeswaran.sagaorchestrator.service;

import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AccountOpeningSagaService {
    private final WebClient webClient;

    public AccountOpeningSagaService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // URLs for user-service and account-service via API Gateway
    private static final String USER_SERVICE_URL = "http://localhost:8080/api/users";
    private static final String ACCOUNT_SERVICE_URL = "http://localhost:8080/api/accounts";

    public AccountOpeningSagaResponse executeAccountOpeningSaga(AccountOpeningSagaRequest request) {
        try {
            // 1. Create user in user-service
            var userPayload = new UserCreateRequest(request.username(), request.email(), request.fullName());
            var userResponse = webClient.post()
                    .uri(USER_SERVICE_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(userPayload)
                    .retrieve()
                    .bodyToMono(UserCreateResponse.class)
                    .block();
            if (userResponse == null || userResponse.id() == null) {
                return new AccountOpeningSagaResponse("FAILED", "User creation failed", null, null);
            }
            String userId = userResponse.id();

            // 2. Create account in account-service
            var accountPayload = new AccountCreateRequest(null, null, request.accountType(), userId, 0.0, "ACTIVE");
            var accountResponse = webClient.post()
                    .uri(ACCOUNT_SERVICE_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(accountPayload)
                    .retrieve()
                    .bodyToMono(AccountCreateResponse.class)
                    .block();
            if (accountResponse == null || accountResponse.accountId() == null) {
                // Compensation: delete user (not implemented)
                return new AccountOpeningSagaResponse("FAILED", "Account creation failed, user will be deleted (compensation not implemented)", userId, null);
            }
            String accountId = accountResponse.accountId();

            // Success
            return new AccountOpeningSagaResponse("SUCCESS", "User and account created successfully", userId, accountId);
        } catch (Exception ex) {
            return new AccountOpeningSagaResponse("FAILED", "Saga failed: " + ex.getMessage(), null, null);
        }
    }

    // DTOs for user-service and account-service requests/responses
    private record UserCreateRequest(String username, String email, String fullName) {}
    private record UserCreateResponse(String id, String username, String email, String fullName) {}
    private record AccountCreateRequest(String accountId, String accountNumber, String accountType, String userId, double balance, String status) {}
    private record AccountCreateResponse(String accountId, String accountNumber, String accountType, String userId, double balance, String status) {}
}
