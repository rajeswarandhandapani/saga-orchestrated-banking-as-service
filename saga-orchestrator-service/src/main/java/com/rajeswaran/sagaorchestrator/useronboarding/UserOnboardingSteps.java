package com.rajeswaran.sagaorchestrator.useronboarding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum defining all steps in the user onboarding saga.
 * 
 * This enum provides type safety and eliminates magic strings for step names.
 * Each step represents a specific phase in the user onboarding process.
 * 
 * @author Rajeswaran
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum UserOnboardingSteps {
    
    /**
     * Initial step that records the start of the saga with user details.
     */
    SAGA_INITIATED("SagaInitiated"),
    
    /**
     * Step for creating user account in the user service.
     */
    CREATE_USER("CreateUser"),
    
    /**
     * Step for opening bank account in the account service.
     */
    OPEN_ACCOUNT("OpenAccount"),
    
    /**
     * Step for sending welcome notification to the user.
     */
    SEND_NOTIFICATION("SendNotification"),
    
    /**
     * Compensation step for deleting user when account opening fails.
     */
    DELETE_USER("DeleteUser");
    
    /**
     * The actual step name used in the database and logs.
     */
    private final String stepName;
    
}
