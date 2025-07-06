package com.rajeswaran.sagaorchestrator.saga.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object to initiate a payment processing saga.
 * Contains all necessary information to process a payment transaction.
 *
 * @author Rajeswaran
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private double amount;
    private String description;
}
