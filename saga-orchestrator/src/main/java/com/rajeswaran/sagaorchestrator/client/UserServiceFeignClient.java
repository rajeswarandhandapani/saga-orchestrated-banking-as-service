package com.rajeswaran.sagaorchestrator.client;

import com.rajeswaran.sagaorchestrator.dto.UserCreateRequest;
import com.rajeswaran.sagaorchestrator.dto.UserCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:8080/api/users")
public interface UserServiceFeignClient {
    @PostMapping(consumes = "application/json")
    UserCreateResponse createUser(
        @RequestBody UserCreateRequest request,
        @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    );
}

