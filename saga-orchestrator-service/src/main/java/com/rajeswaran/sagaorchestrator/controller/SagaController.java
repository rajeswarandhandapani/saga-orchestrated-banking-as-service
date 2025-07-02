package com.rajeswaran.sagaorchestrator.controller;

import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import com.rajeswaran.sagaorchestrator.service.SagaOrchestrator;
import com.rajeswaran.sagaorchestrator.useronboarding.UserOnboardingSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
public class SagaController {

    private final SagaOrchestrator sagaOrchestrator;
    private final UserOnboardingSaga userOnboardingSaga;

    @PostMapping("/start/user-onboarding")
    public ResponseEntity<String> startUserOnboardingSaga() {
        log.info("Received request to start user onboarding saga");

        String username = SecurityUtil.getCurrentUsername();
        String email = null;
        String fullName = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            email = jwt.getClaimAsString("email");
            fullName = jwt.getClaimAsString("name");
        }

        UserDTO userDTO = UserDTO.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .build();

        // Use new command/event pattern
        Map<String, Object> payload = new HashMap<>();
        payload.put("user", userDTO);
        
        SagaInstance sagaInstance = sagaOrchestrator.startSaga("user-onboarding-saga", payload);
        
        // Delegate to UserOnboardingSaga for command/event orchestration
        userOnboardingSaga.startUserOnboarding(sagaInstance.getId(), userDTO);

        log.info("User onboarding saga {} started for user: {}", sagaInstance.getId(), username);
        return ResponseEntity.accepted().body("User onboarding process started with saga ID: " + sagaInstance.getId());
    }
}
