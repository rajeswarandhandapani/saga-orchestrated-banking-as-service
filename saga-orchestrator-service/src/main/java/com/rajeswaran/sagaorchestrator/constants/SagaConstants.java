package com.rajeswaran.sagaorchestrator.constants;

/**
 * Constants for saga names used throughout the saga orchestrator service.
 * 
 * Using constants instead of magic strings provides:
 * - Type safety and compile-time checking
 * - IDE support for auto-completion and refactoring
 * - Single source of truth for saga names
 * - Better maintainability
 * 
 * @author Rajeswaran
 * @since 1.0.0
 */
public final class SagaConstants {
    private SagaConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * User onboarding saga name used for creating user accounts,
     * opening bank accounts, and sending welcome notifications.
     */
    public static final String USER_ONBOARDING_SAGA = "user-onboarding-saga";

    public enum SagaStepStatus {
        STARTED,
        COMPLETED,
        FAILED,
        COMPENSATED
    }

    public enum SagaStatus {
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        ROLLED_BACK
    }
}
