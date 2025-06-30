package com.rajeswaran.saga.service;

import com.rajeswaran.saga.entity.SagaInstance;

import java.util.Map;

public interface SagaOrchestrator {

    SagaInstance startSaga(String sagaName, Map<String, Object> payload);

    void handleReply(String replyDestination, String payload, boolean isFailure);

}
