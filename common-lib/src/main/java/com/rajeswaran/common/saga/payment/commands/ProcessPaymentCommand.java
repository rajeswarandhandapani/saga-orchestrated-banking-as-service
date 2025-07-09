package com.rajeswaran.common.saga.payment.commands;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.saga.command.BaseCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to process a validated payment.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProcessPaymentCommand extends BaseCommand {

    Payment payment;

    public ProcessPaymentCommand(String commandId, Long sagaId, Instant timestamp, Payment payment) {
        super(commandId, sagaId, timestamp);
        this.payment = payment;
    }

    public static ProcessPaymentCommand create(Long sagaId, Payment payment) {
        return new ProcessPaymentCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            Instant.now(),
            payment
        );
    }
}
