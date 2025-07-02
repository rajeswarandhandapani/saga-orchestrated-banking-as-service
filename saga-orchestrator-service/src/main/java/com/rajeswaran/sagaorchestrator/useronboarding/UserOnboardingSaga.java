package com.rajeswaran.sagaorchestrator.useronboarding;

import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.saga.SagaId;
import com.rajeswaran.common.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.useronboarding.commands.OpenAccountCommand;
import com.rajeswaran.common.useronboarding.commands.SendWelcomeNotificationCommand;
import com.rajeswaran.common.useronboarding.events.AccountOpenedEvent;
import com.rajeswaran.common.useronboarding.events.AccountOpenFailedEvent;
import com.rajeswaran.common.useronboarding.events.UserCreatedEvent;
import com.rajeswaran.common.useronboarding.events.UserCreationFailedEvent;
import com.rajeswaran.common.useronboarding.events.WelcomeNotificationSentEvent;
import com.rajeswaran.common.useronboarding.events.WelcomeNotificationFailedEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.sagaorchestrator.model.SagaStepStatus;
import com.rajeswaran.sagaorchestrator.model.SagaStatus;
import com.rajeswaran.sagaorchestrator.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Self-orchestrating User Onboarding Saga using command/event pattern.
 * 
 * Flow:
 * 1. Produces CreateUserCommand → Listens for UserCreatedEvent/UserCreationFailedEvent
 * 2. Produces OpenAccountCommand → Listens for AccountOpenedEvent/AccountOpenFailedEvent  
 * 3. Produces SendWelcomeNotificationCommand → Listens for WelcomeNotificationSentEvent/WelcomeNotificationFailedEvent
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserOnboardingSaga {
    
    private final SagaOrchestrator sagaOrchestrator;
    private final StreamBridge streamBridge;
    
    // === COMMAND PRODUCERS (Triggers commands to other services) ===
    
    public void startUserOnboarding(Long sagaId, UserDTO userDto) {
        log.info("Starting user onboarding saga {} for user: {}", sagaId, userDto.getUsername());
        
        // Record the initial step with user payload
        sagaOrchestrator.recordStep(sagaId, "SagaInitiated", SagaStepStatus.COMPLETED, 
            String.format("User: %s, Email: %s, FullName: %s", 
                userDto.getUsername(), userDto.getEmail(), userDto.getFullName()));
        
        triggerCreateUserCommand(sagaId, userDto);
    }
    
    private void triggerCreateUserCommand(Long sagaId, UserDTO userDto) {
        log.info("Triggering CreateUserCommand for saga {} and user: {}", sagaId, userDto.getUsername());
        
        CreateUserCommand command = CreateUserCommand.create(
            SagaId.of(String.valueOf(sagaId)),
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            userDto.getUsername(),
            userDto.getEmail(),
            userDto.getFullName(),
            "defaultPassword123" // In real scenario, this would come from the request
        );
        
        streamBridge.send("createUserCommand-out-0", command);
        sagaOrchestrator.recordStep(sagaId, "CreateUser", SagaStepStatus.STARTED);
    }
    
    private void triggerOpenAccountCommand(Long sagaId, String userId) {
        log.info("Triggering OpenAccountCommand for saga {} and userId: {}", sagaId, userId);
        
        OpenAccountCommand command = OpenAccountCommand.create(
            SagaId.of(String.valueOf(sagaId)),
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            userId,
            "SAVINGS"
        );
        
        streamBridge.send("openAccountCommand-out-0", command);
        sagaOrchestrator.recordStep(sagaId, "OpenAccount", SagaStepStatus.STARTED);
    }
    
    private void triggerSendWelcomeNotificationCommand(Long sagaId, String userId, String email, String fullName) {
        log.info("Triggering SendWelcomeNotificationCommand for saga {} and user: {}", sagaId, userId);
        
        SendWelcomeNotificationCommand command = SendWelcomeNotificationCommand.create(
            SagaId.of(String.valueOf(sagaId)),
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            userId,
            email,
            fullName
        );
        
        streamBridge.send("sendWelcomeNotificationCommand-out-0", command);
        sagaOrchestrator.recordStep(sagaId, "SendNotification", SagaStepStatus.STARTED);
    }
    
    // === EVENT LISTENERS (Consumes events from other services) ===
    
    @Bean
    public Consumer<Message<UserCreatedEvent>> userCreatedEvent() {
        return message -> {
            UserCreatedEvent event = message.getPayload();
            log.info("User created successfully for saga {}, userId: {}", event.getSagaId().value(), event.getUserId());
            
            sagaOrchestrator.recordStep(Long.valueOf(event.getSagaId().value()), "CreateUser", SagaStepStatus.COMPLETED);
            
            // Proceed to next step: Open Account
            triggerOpenAccountCommand(Long.valueOf(event.getSagaId().value()), event.getUserId());
        };
    }
    
    @Bean
    public Consumer<Message<UserCreationFailedEvent>> userCreationFailedEvent() {
        return message -> {
            UserCreationFailedEvent event = message.getPayload();
            log.error("User creation failed for saga {}: {}", event.getSagaId().value(), event.getErrorMessage());
            
            sagaOrchestrator.recordStep(Long.valueOf(event.getSagaId().value()), "CreateUser", SagaStepStatus.FAILED);
            sagaOrchestrator.compensate(Long.valueOf(event.getSagaId().value()));
        };
    }
    
    @Bean
    public Consumer<Message<AccountOpenedEvent>> accountOpenedEvent() {
        return message -> {
            AccountOpenedEvent event = message.getPayload();
            log.info("Account opened successfully for saga {}, accountId: {}", event.getSagaId().value(), event.getAccountId());
            
            sagaOrchestrator.recordStep(Long.valueOf(event.getSagaId().value()), "OpenAccount", SagaStepStatus.COMPLETED);
            
            // Proceed to next step: Send Welcome Notification
            // We need email and fullName, but they're not in AccountOpenedEvent, so we'll use userId for now
            triggerSendWelcomeNotificationCommand(Long.valueOf(event.getSagaId().value()), event.getUserId(), "user@example.com", "User Name");
        };
    }
    
    @Bean
    public Consumer<Message<AccountOpenFailedEvent>> accountOpenFailedEvent() {
        return message -> {
            AccountOpenFailedEvent event = message.getPayload();
            log.error("Account opening failed for saga {}: {}", event.getSagaId().value(), event.getErrorMessage());
            
            sagaOrchestrator.recordStep(Long.valueOf(event.getSagaId().value()), "OpenAccount", SagaStepStatus.FAILED);
            sagaOrchestrator.compensate(Long.valueOf(event.getSagaId().value()));
        };
    }
    
    @Bean
    public Consumer<Message<WelcomeNotificationSentEvent>> welcomeNotificationSentEvent() {
        return message -> {
            WelcomeNotificationSentEvent event = message.getPayload();
            log.info("Welcome notification sent for saga {}", event.getSagaId().value());
            
            sagaOrchestrator.recordStep(Long.valueOf(event.getSagaId().value()), "SendNotification", SagaStepStatus.COMPLETED);
            
            // Saga completed successfully
            sagaOrchestrator.updateSagaState(Long.valueOf(event.getSagaId().value()), SagaStatus.COMPLETED);
            log.info("User onboarding saga {} completed successfully", event.getSagaId().value());
        };
    }
    
    @Bean
    public Consumer<Message<WelcomeNotificationFailedEvent>> welcomeNotificationFailedEvent() {
        return message -> {
            WelcomeNotificationFailedEvent event = message.getPayload();
            log.warn("Welcome notification failed for saga {}: {}", event.getSagaId().value(), event.getErrorMessage());
            
            // Note: Notification failure might not require compensation
            // depending on business requirements - mark as completed with warnings
            sagaOrchestrator.recordStep(Long.valueOf(event.getSagaId().value()), "SendNotification", SagaStepStatus.FAILED);
            sagaOrchestrator.updateSagaState(Long.valueOf(event.getSagaId().value()), SagaStatus.COMPLETED);
            log.info("User onboarding saga {} completed with notification failure (non-critical)", event.getSagaId().value());
        };
    }
}
