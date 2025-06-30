package com.rajeswaran.saga.controller;

import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.util.SecurityUtil;
import com.rajeswaran.saga.service.SagaOrchestrator;
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

        Map<String, Object> payload = new HashMap<>();
        payload.put("user", userDTO);

        sagaOrchestrator.startSaga("user-onboarding", payload);

        log.info("Saga 'user-onboarding' started for user: {}", username);
        return ResponseEntity.accepted().body("User onboarding process started.");
    }
}
