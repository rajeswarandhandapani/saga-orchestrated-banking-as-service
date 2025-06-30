package com.rajeswaran.saga.listener;

import com.rajeswaran.saga.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class SagaReplyListener {

    private final SagaOrchestrator sagaOrchestrator;

    private static final String SPRING_CLOUD_STREAM_DESTINATION = "spring_cloud_stream_destination";

    // --- User Onboarding Saga Replies ---

    @Bean
    public Consumer<Message<String>> userCreatedReply() {
        return message -> sagaOrchestrator.handleReply(
                (String) message.getHeaders().get(SPRING_CLOUD_STREAM_DESTINATION),
                message.getPayload(),
                false);
    }

    @Bean
    public Consumer<Message<String>> userCreationFailedReply() {
        return message -> sagaOrchestrator.handleReply(
                (String) message.getHeaders().get(SPRING_CLOUD_STREAM_DESTINATION),
                message.getPayload(),
                true);
    }

    @Bean
    public Consumer<Message<String>> accountOpenedReply() {
        return message -> sagaOrchestrator.handleReply(
                (String) message.getHeaders().get(SPRING_CLOUD_STREAM_DESTINATION),
                message.getPayload(),
                false);
    }

    @Bean
    public Consumer<Message<String>> accountOpenFailedReply() {
        return message -> sagaOrchestrator.handleReply(
                (String) message.getHeaders().get(SPRING_CLOUD_STREAM_DESTINATION),
                message.getPayload(),
                true);
    }

    @Bean
    public Consumer<Message<String>> notificationSentReply() {
        return message -> sagaOrchestrator.handleReply(
                (String) message.getHeaders().get(SPRING_CLOUD_STREAM_DESTINATION),
                message.getPayload(),
                false);
    }

    @Bean
    public Consumer<Message<String>> notificationFailedReply() {
        return message -> sagaOrchestrator.handleReply(
                (String) message.getHeaders().get(SPRING_CLOUD_STREAM_DESTINATION),
                message.getPayload(),
                true);
    }
}
