package com.rajeswaran.common.saga.payment.commands;

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
 * Command to update account balances for a processed payment.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdateAccountBalanceCommand extends BaseCommand {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String destinationAccountNumber;

    @Positive
    private double amount;

    private String description;

    public UpdateAccountBalanceCommand(String commandId, Long sagaId, Instant timestamp,
                                      String paymentId, String sourceAccountNumber, String destinationAccountNumber,
                                      double amount, String description) {
        super(commandId, sagaId, timestamp);
        this.paymentId = paymentId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.description = description;
    }

    public static UpdateAccountBalanceCommand create(long sagaId, String paymentId,
                                                    String sourceAccountNumber, String destinationAccountNumber,
                                                    double amount, String description) {
        return new UpdateAccountBalanceCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            Instant.now(),
            paymentId,
            sourceAccountNumber,
            destinationAccountNumber,
            amount,
            description
        );
    }
}
