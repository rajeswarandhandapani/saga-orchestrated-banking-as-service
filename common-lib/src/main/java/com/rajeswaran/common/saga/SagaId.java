package com.rajeswaran.common.saga;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Type-safe saga identifier using Java 21 record.
 * Prevents mixing up saga IDs with other string values.
 */
public record SagaId(
    @NotBlank
    String value,
    
    @NotNull
    SagaType type
) {
    
    /**
     * Creates a new SagaId with validation.
     */
    public SagaId {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Saga ID value cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Saga type cannot be null");
        }
    }
    
    /**
     * Creates a SagaId from a string value and type.
     */
    public static SagaId of(String value, SagaType type) {
        return new SagaId(value.trim(), type);
    }
    
    /**
     * Creates a new unique SagaId for the given type.
     */
    public static SagaId generate(SagaType type) {
        return new SagaId(java.util.UUID.randomUUID().toString(), type);
    }
    
    @Override
    public String toString() {
        return String.format("%s[%s]", type, value);
    }
}
