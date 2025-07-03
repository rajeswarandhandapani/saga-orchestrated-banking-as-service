package com.rajeswaran.sagaorchestrator.controller;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import com.rajeswaran.sagaorchestrator.saga.useronboarding.UserOnboardingSaga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
public class SagaController {

    private final UserOnboardingSaga userOnboardingSaga;

    @PostMapping("/start/user-onboarding")
    public ResponseEntity<String> startUserOnboardingSaga(@RequestBody User user) {
        log.info("Received request to start user onboarding saga");

        // String username = SecurityUtil.getCurrentUsername();
        // String email = null;
        // String fullName = null;

        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
        //     email = jwt.getClaimAsString("email");
        //     fullName = jwt.getClaimAsString("name");
        // }


        // Use Saga interface to start saga with payload - this will automatically trigger the first command
        SagaInstance sagaInstance = userOnboardingSaga.startSaga(user);

        log.info("User onboarding saga {} started for user: {}", sagaInstance.getId(), user);
        return ResponseEntity.accepted().body("User onboarding process started with saga ID: " + sagaInstance.getId());
    }
}
