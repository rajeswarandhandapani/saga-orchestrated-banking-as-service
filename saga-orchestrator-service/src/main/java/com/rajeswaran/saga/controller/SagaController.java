package com.rajeswaran.saga.controller;

import com.rajeswaran.saga.dto.StartSagaRequest;
import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaOrchestrator sagaOrchestrator;

    @PostMapping
    public ResponseEntity<SagaInstance> startSaga(@RequestBody StartSagaRequest request) {
        SagaInstance sagaInstance = sagaOrchestrator.startSaga(request.getSagaName(), request.getPayload());
        return ResponseEntity.ok(sagaInstance);
    }
}
