package com.rajeswaran.common.saga.payment.commands;

import com.rajeswaran.common.saga.command.BaseCommand;
import com.rajeswaran.common.saga.SagaId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to record transaction details for audit trail.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RecordTransactionCommand extends BaseCommand {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    @Positive
    private double amount;

    private String description;

    private String transactionType;

    public RecordTransactionCommand(String commandId, SagaId sagaId, String correlationId, Instant timestamp,
                                   String paymentId, String sourceAccountNumber, String destinationAccountNumber,
                                   double amount, String description, String transactionType) {
        super(commandId, sagaId, correlationId, timestamp);
        this.paymentId = paymentId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.description = description;
        this.transactionType = transactionType;
    }

    public static RecordTransactionCommand create(SagaId sagaId, String correlationId, String paymentId,
                                                 String sourceAccountNumber, String destinationAccountNumber,
                                                 double amount, String description, String transactionType) {
        return new RecordTransactionCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            correlationId,
            Instant.now(),
            paymentId,
            sourceAccountNumber,
            destinationAccountNumber,
            amount,
            description,
            transactionType
        );
    }
}
