package com.rajeswaran.common.command;

import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Simple command interface for saga operations.
 */
public interface Command {
    
    @NotNull
    String getCommandId();
    
    @NotNull
    SagaId getSagaId();
    
    @NotNull
    String getCorrelationId();
    
    @NotNull
    Instant getTimestamp();
    
    @NotNull
    String getCommandType();
}
