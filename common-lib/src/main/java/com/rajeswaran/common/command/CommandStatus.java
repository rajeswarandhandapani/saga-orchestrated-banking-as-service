package com.rajeswaran.common.command;

/**
 * Enumeration of possible command states in the system.
 * Represents the lifecycle of a command from creation to completion.
 */
public enum CommandStatus {
    /**
     * Command has been created but not yet sent.
     */
    CREATED,
    
    /**
     * Command has been sent to the target service.
     */
    SENT,
    
    /**
     * Command is being processed by the target service.
     */
    PROCESSING,
    
    /**
     * Command has been successfully processed.
     */
    SUCCESS,
    
    /**
     * Command processing failed and can be retried.
     */
    FAILED_RETRYABLE,
    
    /**
     * Command processing failed permanently.
     */
    FAILED_PERMANENT,
    
    /**
     * Command was cancelled before completion.
     */
    CANCELLED,
    
    /**
     * Command processing timed out.
     */
    TIMEOUT
}
