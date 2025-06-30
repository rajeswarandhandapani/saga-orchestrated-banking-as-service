package com.rajeswaran.saga.config;

import com.rajeswaran.saga.definition.SagaDefinition;
import com.rajeswaran.saga.definition.SagaDefinitionRegistry;
import com.rajeswaran.saga.definition.SagaStepDefinition;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SagaConfig {

    private final SagaDefinitionRegistry registry;

    @PostConstruct
    public void registerSagas() {

        // User Onboarding Saga - using command/reply pattern
        SagaDefinition userOnboardingSaga = SagaDefinition.builder()
                .sagaName("user-onboarding-saga")
                .steps(List.of(
                        // Step 1: Create User
                        SagaStepDefinition.builder()
                                .commandDestination("createUserCommand-out-0")
                                .replyDestination("userCreatedReply-in-0")
                                .compensationDestination("deleteUserCommand-out-0") // Corrected
                                .build(),

                        // Step 2: Open Account
                        SagaStepDefinition.builder()
                                .commandDestination("accountOpenCommand-out-0")
                                .replyDestination("accountOpenedReply-in-0")
                                .compensationDestination("closeAccountCommand-out-0") // Corrected
                                .build(),

                        // Step 3: Send Welcome Notification (Compensation might be to log or do nothing)
                        SagaStepDefinition.builder()
                                .commandDestination("sendNotificationCommand-out-0")
                                .replyDestination("notificationSentReply-in-0")
                                .compensationDestination("cancelNotificationCommand-out-0") // Corrected
                                .build()
                ))
                .build();

        registry.registerSaga(userOnboardingSaga);
    }
}
