package com.rajeswaran.common.command;

import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Interface for commands that are part of a saga workflow.
 * Extends the base Command interface with saga-specific metadata.
 * 
 * All saga commands must be associated with a saga instance and provide
 * compensation information for rollback scenarios.
 */
public non-sealed interface SagaCommand extends Command {
    
    /**
     * The saga instance this command belongs to.
     * @return saga ID for tracking the overall workflow
     */
    @Valid
    @NotNull
    SagaId sagaId();
    
    /**
     * The step number within the saga workflow.
     * Used for ordering and tracking progress.
     * @return step number starting from 1
     */
    @NotNull
    Integer stepNumber();
    
    /**
     * Whether this command can be compensated if the saga needs to rollback.
     * @return true if compensation is supported
     */
    boolean isCompensatable();
    
    /**
     * The compensation command type to execute if this command needs to be rolled back.
     * Only required if isCompensatable() returns true.
     * @return compensation command type, or null if not compensatable
     */
    String compensationCommandType();
    
    /**
     * Timeout for this command in milliseconds.
     * After this time, the command will be considered failed.
     * @return timeout in milliseconds
     */
    @NotNull
    Long timeoutMs();
}
