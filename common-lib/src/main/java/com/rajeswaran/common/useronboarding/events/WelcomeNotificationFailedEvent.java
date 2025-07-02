package com.rajeswaran.common.useronboarding.events;

import com.rajeswaran.common.event.BaseEvent;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Event indicating welcome notification sending failed.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WelcomeNotificationFailedEvent extends BaseEvent {
    @NotBlank
    private String userId;
    @NotBlank
    private String email;

    public static WelcomeNotificationFailedEvent create(SagaId sagaId, String correlationId,
                                                       String userId, String email, String errorMessage) {
        return WelcomeNotificationFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .success(false)
            .errorMessage(errorMessage)
            .userId(userId)
            .email(email)
            .build();
    }
}
