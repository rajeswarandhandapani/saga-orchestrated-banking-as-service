package com.rajeswaran.sagaorchestrator.saga.payment;

/**
 * Enumeration of payment processing saga steps.
 *
 * Defines the sequence of steps in the payment processing saga flow:
 * 1. VALIDATE_PAYMENT - Validate payment details and business rules
 * 2. PROCESS_PAYMENT - Process the payment transaction
 * 3. RECORD_TRANSACTION - Record transaction details for audit trail
 * 4. SEND_NOTIFICATION - Send payment confirmation notification
 *
 * @author Rajeswaran
 * @since 1.0.0
 */
public enum PaymentProcessingSteps {

    /**
     * First step: Validate payment details including account verification,
     * balance checks, and business rule validation.
     */
    VALIDATE_PAYMENT("validate-payment"),

    /**
     * Second step: Process the validated payment transaction.
     */
    PROCESS_PAYMENT("process-payment"),

    /**
     * Third step: Record transaction details in the transaction service for audit trail.
     */
    RECORD_TRANSACTION("record-transaction"),

    /**
     * Final step: Send payment confirmation notification to the user.
     */
    SEND_NOTIFICATION("send-notification");

    private final String stepName;

    PaymentProcessingSteps(String stepName) {
        this.stepName = stepName;
    }

    public String getStepName() {
        return stepName;
    }

    /**
     * Get the next step in the payment processing flow.
     * Returns null if this is the last step.
     */
    public PaymentProcessingSteps getNextStep() {
        PaymentProcessingSteps[] steps = values();
        int currentIndex = this.ordinal();

        if (currentIndex < steps.length - 1) {
            return steps[currentIndex + 1];
        }

        return null; // No next step (saga complete)
    }

    /**
     * Check if this is the first step in the saga.
     */
    public boolean isFirstStep() {
        return this == VALIDATE_PAYMENT;
    }

    /**
     * Check if this is the last step in the saga.
     */
    public boolean isLastStep() {
        return this == SEND_NOTIFICATION;
    }
}
