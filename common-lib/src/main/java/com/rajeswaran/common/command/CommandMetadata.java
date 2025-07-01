package com.rajeswaran.common.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable metadata record for commands using Java 21 record feature.
 * Contains essential information for command processing, routing, and auditing.
 */
public record CommandMetadata(
    @NotBlank
    String sourceService,
    
    @NotBlank  
    String targetService,
    
    @NotNull
    CommandStatus status,
    
    @NotNull
    Instant createdAt,
    
    Instant processedAt,
    
    String processedBy,
    
    @NotNull
    Map<String, String> headers,
    
    String description
) {
    
    /**
     * Creates a new CommandMetadata for a command being initiated.
     */
    public static CommandMetadata forNewCommand(
            String sourceService, 
            String targetService, 
            String description,
            Map<String, String> headers) {
        return new CommandMetadata(
            sourceService,
            targetService, 
            CommandStatus.CREATED,
            Instant.now(),
            null,
            null,
            headers != null ? Map.copyOf(headers) : Map.of(),
            description
        );
    }
    
    /**
     * Creates a copy with updated status and processing information.
     */
    public CommandMetadata withProcessingInfo(CommandStatus status, String processedBy) {
        return new CommandMetadata(
            sourceService,
            targetService,
            status,
            createdAt,
            Instant.now(),
            processedBy,
            headers,
            description
        );
    }
}
