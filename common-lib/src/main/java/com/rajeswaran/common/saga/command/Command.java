package com.rajeswaran.common.saga.command;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Simple command interface for saga operations.
 */
public interface Command {
    
    @NotNull
    String getCommandId();
    
    @NotNull
    Long getSagaId();
    
    @NotNull
    String getCorrelationId();
    
    @NotNull
    Instant getTimestamp();
    
    @NotNull
    String getCommandType();
}
