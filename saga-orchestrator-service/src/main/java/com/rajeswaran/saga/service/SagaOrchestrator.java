package com.rajeswaran.saga.service;

import com.rajeswaran.saga.entity.SagaInstance;

public interface SagaOrchestrator {

    SagaInstance startSaga(String sagaName, String payload);

    void handleReply(String replyDestination, String payload, boolean isFailure);

}
