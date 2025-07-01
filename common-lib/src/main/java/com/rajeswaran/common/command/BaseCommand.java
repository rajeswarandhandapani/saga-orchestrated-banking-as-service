package com.rajeswaran.common.command;

import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Base abstract class for all commands with common fields.
 * Provides shared functionality and ensures Spring Cloud Stream compatibility.
 */
@Getter
@AllArgsConstructor
public abstract class BaseCommand implements Command {
    
    @NotNull
    private final String commandId;
    
    @NotNull
    private final SagaId sagaId;
    
    @NotNull
    private final String correlationId;
    
    @NotNull
    private final Instant timestamp;
    
    @Override
    public String getCommandType() {
        return this.getClass().getSimpleName();
    }
}
