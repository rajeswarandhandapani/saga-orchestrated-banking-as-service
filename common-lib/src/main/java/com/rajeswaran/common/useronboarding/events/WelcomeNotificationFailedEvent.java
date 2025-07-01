package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.Instant;

/**
 * Event indicating welcome notification sending failed.
 */
@Getter
public class WelcomeNotificationFailedEvent extends BaseEvent {
    
    @NotBlank
    private final String userId;
    
    @NotBlank
    private final String email;
    
    public WelcomeNotificationFailedEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                        String userId, String email, String errorMessage) {
        super(eventId, sagaId, correlationId, timestamp, false, errorMessage);
        this.userId = userId;
        this.email = email;
    }
    
    public static WelcomeNotificationFailedEvent create(SagaId sagaId, String correlationId,
                                                      String userId, String email, String errorMessage) {
        return new WelcomeNotificationFailedEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            email,
            errorMessage
        );
    }
}
