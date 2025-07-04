package com.rajeswaran.common.saga.payment.events;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.saga.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event indicating transaction recording has failed.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransactionFailedEvent extends BaseEvent {

    Payment payment;
    private String reason;


    public static TransactionFailedEvent create(Long sagaId, String correlationId,  Payment payment, String reason) {
        return TransactionFailedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .sagaId(sagaId)
            .correlationId(correlationId)
            .timestamp(java.time.Instant.now())
            .payment(payment)
            .reason(reason)
            .build();
    }
}
