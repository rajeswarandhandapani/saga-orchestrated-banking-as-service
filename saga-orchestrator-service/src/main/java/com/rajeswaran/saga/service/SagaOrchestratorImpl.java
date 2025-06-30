package com.rajeswaran.saga.service;

import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.model.SagaStatus;
import com.rajeswaran.saga.repository.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final SagaInstanceRepository sagaInstanceRepository;

    @Override
    @Transactional
    public SagaInstance startSaga(String sagaName, String payload) {
        SagaInstance sagaInstance = SagaInstance.builder()
                .sagaName(sagaName)
                .status(SagaStatus.STARTED)
                .build();
        return sagaInstanceRepository.save(sagaInstance);
    }
}
