package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Event indicating welcome notification was successfully sent.
 */
public class WelcomeNotificationSentEvent extends BaseEvent {
    
    @NotBlank
    private final String userId;
    
    @NotBlank
    private final String email;
    
    public WelcomeNotificationSentEvent(String eventId, SagaId sagaId, String correlationId, Instant timestamp,
                                      String userId, String email) {
        super(eventId, sagaId, correlationId, timestamp, true, null);
        this.userId = userId;
        this.email = email;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public static WelcomeNotificationSentEvent create(SagaId sagaId, String correlationId,
                                                    String userId, String email) {
        return new WelcomeNotificationSentEvent(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            userId,
            email
        );
    }
}
