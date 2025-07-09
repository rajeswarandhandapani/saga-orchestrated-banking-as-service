package com.rajeswaran.common.saga.payment.events;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.saga.event.BaseEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating a payment has been successfully validated.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PaymentValidatedEvent extends BaseEvent {

    Payment payment;

    public static PaymentValidatedEvent create(Long sagaId, Payment payment) {
        return PaymentValidatedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .timestamp(java.time.Instant.now())
            .payment(payment)
            .build();
    }
}
