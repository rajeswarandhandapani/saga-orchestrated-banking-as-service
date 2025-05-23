package com.rajeswaran.sagaorchestrator.controller;

import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaResponse;
import com.rajeswaran.sagaorchestrator.service.AccountOpeningSagaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/saga/account-opening")
public class AccountOpeningSagaController {
    @Autowired
    private AccountOpeningSagaService sagaService;

    @PostMapping
    public ResponseEntity<AccountOpeningSagaResponse> startAccountOpeningSaga(
            @RequestBody AccountOpeningSagaRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.info("Received AccountOpeningSaga request: {}", request);
        AccountOpeningSagaResponse response = sagaService.executeAccountOpeningSaga(request, authorizationHeader);
        log.info("Completed AccountOpeningSaga request: status={}, message={}", response.status(), response.message());
        return ResponseEntity.ok(response);
    }
}

