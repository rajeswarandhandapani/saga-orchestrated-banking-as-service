package com.rajeswaran.common.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Base sealed interface for all commands in the system.
 * Uses Java 21 sealed interface to restrict implementations to known command types.
 * 
 * All commands must be immutable and carry metadata for tracing and validation.
 */
public sealed interface Command permits SagaCommand {
    
    /**
     * Unique identifier for this command instance.
     * @return non-null command ID
     */
    @NotNull
    String commandId();
    
    /**
     * Timestamp when the command was created.
     * @return command creation timestamp
     */
    @NotNull
    Instant timestamp();
    
    /**
     * Correlation ID for tracing this command across services.
     * @return correlation ID for distributed tracing
     */
    @NotNull
    String correlationId();
    
    /**
     * Metadata associated with this command.
     * @return validated command metadata
     */
    @Valid
    @NotNull
    CommandMetadata metadata();
    
    /**
     * The type of this command for routing and handling purposes.
     * @return command type identifier
     */
    @NotNull
    String commandType();
}
