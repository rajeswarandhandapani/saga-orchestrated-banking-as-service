package com.rajeswaran.sagaorchestrator.client;

import com.rajeswaran.sagaorchestrator.dto.UserCreateRequest;
import com.rajeswaran.sagaorchestrator.dto.UserCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {
    @PostMapping("/api/users")
    UserCreateResponse createUser(
        @RequestBody UserCreateRequest request,
        @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    );
}

