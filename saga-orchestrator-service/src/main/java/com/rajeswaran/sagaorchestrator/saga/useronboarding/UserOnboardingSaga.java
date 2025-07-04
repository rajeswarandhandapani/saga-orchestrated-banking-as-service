package com.rajeswaran.sagaorchestrator.saga.useronboarding;

import com.rajeswaran.common.entity.User;
import com.rajeswaran.common.saga.useronboarding.commands.CreateUserCommand;
import com.rajeswaran.common.saga.useronboarding.commands.DeleteUserCommand;
import com.rajeswaran.common.saga.useronboarding.commands.OpenAccountCommand;
import com.rajeswaran.common.saga.notification.commands.SendNotificationCommand;
import com.rajeswaran.common.saga.useronboarding.events.*;
import com.rajeswaran.common.util.SagaEventBuilderUtil;
import com.rajeswaran.sagaorchestrator.constants.SagaConstants;
import com.rajeswaran.sagaorchestrator.saga.Saga;
import com.rajeswaran.sagaorchestrator.service.SagaStateManager;
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
public class UserOnboardingSaga extends Saga {
    
    public UserOnboardingSaga(SagaStateManager sagaStateManager, StreamBridge streamBridge) {
        super(sagaStateManager, streamBridge);
    }
    
    @Override
    public String getSagaName() {
        return SagaConstants.USER_ONBOARDING_SAGA;
    }
    
    @Override
    public void startSagaFlow(Long sagaId, Object payload) {
        if (payload instanceof User user) {
            log.info("Starting user onboarding saga flow {} for user: {}", sagaId, user.getUsername());
            triggerCreateUserCommand(sagaId, user);
        } else {
            throw new IllegalArgumentException("UserOnboardingSaga requires User as payload, got: " + 
                (payload != null ? payload.getClass().getSimpleName() : "null"));
        }
    }
    
    @Override
    public void completeSagaFlow(Long sagaId) {
        log.info("User onboarding saga {} completed successfully", sagaId);
    }
    
    // === COMMAND PRODUCERS (Triggers commands to other services) ===
    
    private void triggerCreateUserCommand(Long sagaId, User user) {
        log.info("Triggering CreateUserCommand for saga {} and user: {}", sagaId, user);
        
        
        CreateUserCommand command = CreateUserCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            user
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, UserOnboardingSteps.CREATE_USER.getStepName(), command);
        
        streamBridge.send("createUserCommand-out-0", command);
    }
    
    private void triggerOpenAccountCommand(Long sagaId, User user) {
        log.info("Triggering OpenAccountCommand for saga {} and userId: {}", sagaId, user.getUserId());
        
        OpenAccountCommand command = OpenAccountCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            "SAVINGS",
            user
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, UserOnboardingSteps.OPEN_ACCOUNT.getStepName(), command);

        streamBridge.send("accountOpenCommand-out-0", command);
    }
    
    private void triggerDeleteUserCommand(Long sagaId, String username) {
        log.info("Triggering DeleteUserCommand for saga {} and username: {} (compensation)", sagaId, username);
        
        DeleteUserCommand command = DeleteUserCommand.create(
            sagaId,
            SagaEventBuilderUtil.getCurrentCorrelationId(),
            username
        );

        // Record step as STARTED before publishing command
        startStep(sagaId, UserOnboardingSteps.DELETE_USER.getStepName(), command);
        
        streamBridge.send("deleteUserCommand-out-0", command);
    }
    // === EVENT LISTENERS (Consumes events from other services) ===
     @Bean
    public Consumer<Message<UserCreatedEvent>> userCreatedEvent() {
        return message -> {

            UserCreatedEvent event = message.getPayload();

            log.info("User created successfully for saga {}, event: {}", event.getSagaId(), event);
            
            completeStep(event.getSagaId(), UserOnboardingSteps.CREATE_USER.getStepName(), event);
            
            // Proceed to next step: Open Account
            triggerOpenAccountCommand(event.getSagaId(), event.getUser());
        };
    }

    @Bean
    public Consumer<Message<UserCreationFailedEvent>> userCreationFailedEvent() {
        return message -> {
            UserCreationFailedEvent event = message.getPayload();
            log.error("User creation failed for saga {}: {}", event.getSagaId(), event.getErrorMessage());
            
            failStep(event.getSagaId(), UserOnboardingSteps.CREATE_USER.getStepName(), event);
            failSaga(event.getSagaId());
        };
    }
     @Bean
    public Consumer<Message<AccountOpenedEvent>> accountOpenedEvent() {
        return message -> {
            AccountOpenedEvent event = message.getPayload();

            log.info("Account opened successfully for saga {}, event: {}", event.getSagaId(), event);
            
            completeStep(event.getSagaId(), UserOnboardingSteps.OPEN_ACCOUNT.getStepName(), event);
            
            // Proceed to next step: Send Welcome Notification
            // We need email and fullName, but they're not in AccountOpenedEvent, so we'll use userId for now
            User user = event.getUser();

            String subject = "Welcome to BaaS Banking Service";
            String notificationMessage = String.format("Hello %s,\n\nWelcome to  BaaS Banking service! Your account number is %s.\n\nBest regards,\nYour Company", user.getFullName(), event.getAccount().getAccountNumber());

            triggerSendNotificationCommand(event.getSagaId(), user.getUsername(), subject, notificationMessage);

            // Record step as STARTED before publishing command
            completeSaga(event.getSagaId());

        };
    }

    @Bean
    public Consumer<Message<AccountOpenFailedEvent>> accountOpenFailedEvent() {
        return message -> {
            AccountOpenFailedEvent event = message.getPayload();
            log.error("Account opening failed for saga {}: {}", event.getSagaId(), event.getErrorMessage());
            
            failStep(event.getSagaId(), UserOnboardingSteps.OPEN_ACCOUNT.getStepName(), event);
            
            // Trigger compensation: Delete the user that was created earlier using username
            triggerDeleteUserCommand(event.getSagaId(), event.getUsername());
        };
    }

    @Bean
    public Consumer<Message<UserDeletedEvent>> userDeletedEvent() {
        return message -> {
            UserDeletedEvent event = message.getPayload();
            log.info("User deleted successfully for saga {} (compensation completed)", event.getSagaId());
            
            completeStep(event.getSagaId(), UserOnboardingSteps.DELETE_USER.getStepName(), event);
            failSaga(event.getSagaId());
            log.info("User onboarding saga {} failed and compensation completed", event.getSagaId());
        };
    }

    @Bean
    public Consumer<Message<UserDeletionFailedEvent>> userDeletionFailedEvent() {
        return message -> {
            UserDeletionFailedEvent event = message.getPayload();
            log.error("User deletion failed for saga {} (compensation failed): {}", event.getSagaId(), event.getErrorMessage());
            
            failStep(event.getSagaId(), UserOnboardingSteps.DELETE_USER.getStepName(), event);
            failSaga(event.getSagaId());
            log.error("User onboarding saga {} failed and compensation also failed - manual intervention required", event.getSagaId());
        };
    }
}
