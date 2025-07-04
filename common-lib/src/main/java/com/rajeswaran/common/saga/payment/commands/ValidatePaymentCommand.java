package com.rajeswaran.common.saga.payment.commands;

import com.rajeswaran.common.entity.Payment;
import com.rajeswaran.common.saga.command.BaseCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to validate a payment before processing.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ValidatePaymentCommand extends BaseCommand {

    Payment payment;

    public ValidatePaymentCommand(String commandId, Long sagaId, String correlationId, Instant timestamp, Payment payment) {
        super(commandId, sagaId, correlationId, timestamp);
        this.payment = payment;
    }

    public static ValidatePaymentCommand create(long sagaId, String correlationId, Payment payment) {
        return new ValidatePaymentCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            payment
        );
    }
}
