package com.rajeswaran.saga.listener;

import com.rajeswaran.saga.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class SagaReplyListener {

    private final SagaOrchestrator sagaOrchestrator;

    // Success replies
    @Bean
    public Consumer<String> accountOpenedEvent() {
        return payload -> sagaOrchestrator.handleReply("accountOpenedEvent-in-0", payload, false);
    }

    @Bean
    public Consumer<String> notificationSentEvent() {
        return payload -> sagaOrchestrator.handleReply("notificationSentEvent-in-0", payload, false);
    }

    @Bean
    public Consumer<String> paymentValidatedEvent() {
        return payload -> sagaOrchestrator.handleReply("paymentValidatedEvent-in-0", payload, false);
    }

    @Bean
    public Consumer<String> paymentProcessedEvent() {
        return payload -> sagaOrchestrator.handleReply("paymentProcessedEvent-in-0", payload, false);
    }

    @Bean
    public Consumer<String> transactionRecordedEvent() {
        return payload -> sagaOrchestrator.handleReply("transactionRecordedEvent-in-0", payload, false);
    }

    // Failure replies
    @Bean
    public Consumer<String> accountOpenFailedEvent() {
        return payload -> sagaOrchestrator.handleReply("accountOpenFailedEvent-out-0", payload, true);
    }

    @Bean
    public Consumer<String> notificationCancelEvent() {
        return payload -> sagaOrchestrator.handleReply("notificationCancelEvent-out-0", payload, true);
    }

    @Bean
    public Consumer<String> paymentFailedEvent() {
        return payload -> sagaOrchestrator.handleReply("paymentFailedEvent-out-0", payload, true);
    }

    @Bean
    public Consumer<String> transactionRollbackEvent() {
        return payload -> sagaOrchestrator.handleReply("transactionRollbackEvent-out-0", payload, true);
    }
}
