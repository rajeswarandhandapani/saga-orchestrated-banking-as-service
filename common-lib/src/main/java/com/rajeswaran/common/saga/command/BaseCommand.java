package com.rajeswaran.common.saga.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Base abstract class for all commands with common fields.
 * Provides shared functionality and ensures Spring Cloud Stream compatibility.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class BaseCommand implements Command {
    
    @NotNull
    private String commandId;
    
    @NotNull
    private Long sagaId;
    
    @NotNull
    private Instant timestamp;
    
    @Override
    public String getCommandType() {
        return this.getClass().getSimpleName();
    }
}
