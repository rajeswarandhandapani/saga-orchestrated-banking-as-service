package com.rajeswaran.sagaorchestrator.useronboarding;

import com.rajeswaran.common.model.dto.UserDTO;
import com.rajeswaran.common.saga.SagaId;
import com.rajeswaran.common.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.useronboarding.commands.DeleteUserCommand;
import com.rajeswaran.common.useronboarding.commands.OpenAccountCommand;
import com.rajeswaran.common.useronboarding.commands.SendWelcomeNotificationCommand;
import com.rajeswaran.common.useronboarding.events.AccountOpenedEvent;
import com.rajeswaran.common.useronboarding.events.AccountOpenFailedEvent;
import com.rajeswaran.common.useronboarding.events.UserCreatedEvent;
import com.rajeswaran.common.useronboarding.events.UserCreationFailedEvent;
import com.rajeswaran.common.useronboarding.events.UserDeletedEvent;
import com.rajeswaran.common.useronboarding.events.UserDeletionFailedEvent;
import com.rajeswaran.common.useronboarding.events.WelcomeNotificationSentEvent;
import com.rajeswaran.common.useronboarding.events.WelcomeNotificationFailedEvent;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.sagaorchestrator.service.SagaStateManager;
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
    
    private final SagaStateManager sagaStateManager;
    private final StreamBridge streamBridge;
    
    // === COMMAND PRODUCERS (Triggers commands to other services) ===
    
    public void startUserOnboarding(Long sagaId, UserDTO userDto) {
        log.info("Starting user onboarding saga {} for user: {}", sagaId, userDto.getUsername());
        triggerCreateUserCommand(sagaId, userDto);
    }
    
    private void triggerCreateUserCommand(Long sagaId, UserDTO userDto) {
        log.info("Triggering CreateUserCommand for saga {} and user: {}", sagaId, userDto.getUsername());
        
        
        CreateUserCommand command = CreateUserCommand.create(
            SagaId.of(String.valueOf(sagaId)),
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            userDto
        );

        // Record step as STARTED before publishing command
        sagaStateManager.startStep(sagaId, UserOnboardingSteps.CREATE_USER.getStepName(), command);
        
        streamBridge.send("createUserCommand-out-0", command);
    }
    
    private void triggerOpenAccountCommand(Long sagaId, String userId) {
        log.info("Triggering OpenAccountCommand for saga {} and userId: {}", sagaId, userId);
        
        OpenAccountCommand command = OpenAccountCommand.create(
            SagaId.of(String.valueOf(sagaId)),
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            userId,
            "SAVINGS"
        );

        // Record step as STARTED before publishing command
        sagaStateManager.startStep(sagaId, UserOnboardingSteps.OPEN_ACCOUNT.getStepName(), command);

        streamBridge.send("openAccountCommand-out-0", command);
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

        // Record step as STARTED before publishing command
        sagaStateManager.startStep(sagaId, UserOnboardingSteps.SEND_NOTIFICATION.getStepName(), command);
        
        streamBridge.send("sendWelcomeNotificationCommand-out-0", command);
    }
    
    private void triggerDeleteUserCommand(Long sagaId, String username) {
        log.info("Triggering DeleteUserCommand for saga {} and username: {} (compensation)", sagaId, username);
        
        DeleteUserCommand command = DeleteUserCommand.create(
            SagaId.of(String.valueOf(sagaId)),
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            username
        );

        // Record step as STARTED before publishing command
        sagaStateManager.startStep(sagaId, UserOnboardingSteps.DELETE_USER.getStepName(), command);
        
        streamBridge.send("deleteUserCommand-out-0", command);
    }
    // === EVENT LISTENERS (Consumes events from other services) ===
     @Bean
    public Consumer<Message<UserCreatedEvent>> userCreatedEvent() {
        return message -> {

            UserCreatedEvent event = message.getPayload();

            log.info("User created successfully for saga {}, userId: {}", event.getSagaId().value(), event.getUserId());
            
            sagaStateManager.completeStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.CREATE_USER.getStepName(), event);
            
            // Proceed to next step: Open Account
            triggerOpenAccountCommand(Long.valueOf(event.getSagaId().value()), event.getUserId());
        };
    }

    @Bean
    public Consumer<Message<UserCreationFailedEvent>> userCreationFailedEvent() {
        return message -> {
            UserCreationFailedEvent event = message.getPayload();
            log.error("User creation failed for saga {}: {}", event.getSagaId().value(), event.getErrorMessage());
            
            sagaStateManager.failStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.CREATE_USER.getStepName(), event);
            sagaStateManager.failSaga(Long.valueOf(event.getSagaId().value()));
        };
    }
     @Bean
    public Consumer<Message<AccountOpenedEvent>> accountOpenedEvent() {
        return message -> {
            AccountOpenedEvent event = message.getPayload();

            log.info("Account opened successfully for saga {}, accountId: {}", event.getSagaId().value(), event.getAccountId());
            
            sagaStateManager.completeStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.OPEN_ACCOUNT.getStepName(), event);
            
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
            
            sagaStateManager.failStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.OPEN_ACCOUNT.getStepName(), event);
            
            // Trigger compensation: Delete the user that was created earlier using username
            triggerDeleteUserCommand(Long.valueOf(event.getSagaId().value()), event.getUsername());
        };
    }
     @Bean
    public Consumer<Message<WelcomeNotificationSentEvent>> welcomeNotificationSentEvent() {
        return message -> {
            WelcomeNotificationSentEvent event = message.getPayload();
            log.info("Welcome notification sent for saga {}", event.getSagaId().value());
            
            sagaStateManager.completeStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.SEND_NOTIFICATION.getStepName(), event);
            
            // Saga completed successfully
            sagaStateManager.completeSaga(Long.valueOf(event.getSagaId().value()));
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
            sagaStateManager.failStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.SEND_NOTIFICATION.getStepName(), event);
            sagaStateManager.completeSaga(Long.valueOf(event.getSagaId().value()));
            log.info("User onboarding saga {} completed with notification failure (non-critical)", event.getSagaId().value());
        };
    }

    @Bean
    public Consumer<Message<UserDeletedEvent>> userDeletedEvent() {
        return message -> {
            UserDeletedEvent event = message.getPayload();
            log.info("User deleted successfully for saga {} (compensation completed)", event.getSagaId().value());
            
            sagaStateManager.completeStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.DELETE_USER.getStepName(), event);
            sagaStateManager.failSaga(Long.valueOf(event.getSagaId().value()));
            log.info("User onboarding saga {} failed and compensation completed", event.getSagaId().value());
        };
    }

    @Bean
    public Consumer<Message<UserDeletionFailedEvent>> userDeletionFailedEvent() {
        return message -> {
            UserDeletionFailedEvent event = message.getPayload();
            log.error("User deletion failed for saga {} (compensation failed): {}", event.getSagaId().value(), event.getErrorMessage());
            
            sagaStateManager.failStep(Long.valueOf(event.getSagaId().value()), UserOnboardingSteps.DELETE_USER.getStepName(), event);
            sagaStateManager.failSaga(Long.valueOf(event.getSagaId().value()));
            log.error("User onboarding saga {} failed and compensation also failed - manual intervention required", event.getSagaId().value());
        };
    }
}
