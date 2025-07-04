package com.rajeswaran.payment.client;

import com.rajeswaran.common.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AccountServiceClient {
    private final WebClient webClient;

    @Autowired
    public AccountServiceClient(WebClient.Builder webClientBuilder) {
        // Uses Eureka service name for account-service
        this.webClient = webClientBuilder.baseUrl("http://account-service").build();
    }

    public Mono<Account> getAccountByNumber(String accountNumber) {
        return webClient.get()
                .uri("/api/accounts/{accountNumber}", accountNumber)
                .retrieve()
                .bodyToMono(Account.class);
    }
}
