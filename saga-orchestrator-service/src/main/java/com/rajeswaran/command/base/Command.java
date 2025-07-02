package com.rajeswaran.command.base;

import java.time.LocalDateTime;

/**
 * Marker interface for all commands in the system.
 * Commands represent intent to perform an action and are immutable.
 */
public interface Command {
    
    /**
     * Unique identifier for the command
     */
    String getCommandId();
    
    /**
     * Type of the command (used for routing and handling)
     */
    String getCommandType();
    
    /**
     * Timestamp when the command was created
     */
    LocalDateTime getTimestamp();
    
    /**
     * Saga ID if this command is part of a saga
     */
    Long getSagaId();
    
    /**
     * User who initiated the command
     */
    String getInitiatedBy();
}
