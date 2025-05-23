package com.rajeswaran.sagaorchestrator.controller;

import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaRequest;
import com.rajeswaran.sagaorchestrator.dto.AccountOpeningSagaResponse;
import com.rajeswaran.sagaorchestrator.service.AccountOpeningSagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saga/account-opening")
public class AccountOpeningSagaController {
    @Autowired
    private AccountOpeningSagaService sagaService;

    @PostMapping
    public ResponseEntity<AccountOpeningSagaResponse> startAccountOpeningSaga(@RequestBody AccountOpeningSagaRequest request) {
        AccountOpeningSagaResponse response = sagaService.executeAccountOpeningSaga(request);
        return ResponseEntity.ok(response);
    }
}

