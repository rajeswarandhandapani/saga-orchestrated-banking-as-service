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

    /**
     * Unique identifier for the payment request.
     */
    @NotBlank
    private String paymentId;

    /**
     * Source account number to debit the payment amount.
     */
    @NotBlank
    private String sourceAccountNumber;

    /**
     * Destination account number to credit the payment amount.
     */
    @NotBlank
    private String destinationAccountNumber;

    /**
     * Amount to be transferred (must be positive).
     */
    @Positive
    private double amount;

    /**
     * Description or purpose of the payment.
     */
    private String description;

    /**
     * Username of the user initiating the payment.
     */
    @NotBlank
    private String username;

    /**
     * Email address for sending payment notifications.
     */
    @NotBlank
    private String userEmail;

    /**
     * Optional reference number for the payment.
     */
    private String reference;
}
