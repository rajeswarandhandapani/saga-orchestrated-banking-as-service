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
 * Event indicating welcome notification was successfully sent.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WelcomeNotificationSentEvent extends BaseEvent {
    @NotBlank
    private String userId;
    @NotBlank
    private String email;

    public static WelcomeNotificationSentEvent create(SagaId sagaId, String correlationId,
                                                     String userId, String email) {
        return WelcomeNotificationSentEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .success(true)
            .errorMessage(null)
            .userId(userId)
            .email(email)
            .build();
    }
}
