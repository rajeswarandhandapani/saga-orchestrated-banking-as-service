package com.rajeswaran.sagaorchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.rajeswaran.sagaorchestrator.dto.AccountCreateRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountCreateResponse;

@FeignClient(name = "account-service")
public interface AccountServiceFeignClient {
    @PostMapping("/api/accounts")
    AccountCreateResponse createAccount(
        @RequestBody AccountCreateRequest request,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    );
}
