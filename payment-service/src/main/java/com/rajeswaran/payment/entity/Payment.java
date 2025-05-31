package com.rajeswaran.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private double amount;
    private String status;
    private String reference;
    private String createdBy; // Added field to track which user created this payment
}
