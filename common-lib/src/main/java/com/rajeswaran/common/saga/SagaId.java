package com.rajeswaran.common.saga;

import jakarta.validation.constraints.NotBlank;

/**
 * Simple saga identifier wrapper.
 */
public record SagaId(@NotBlank String value) {
    
    /**
     * Creates a new unique SagaId.
     */
    public static SagaId generate() {
        return new SagaId(java.util.UUID.randomUUID().toString());
    }
    
    /**
     * Creates a SagaId from a string value.
     */
    public static SagaId of(String value) {
        return new SagaId(value);
    }
}
