package com.rajeswaran.sagaorchestrator.dto;

public record AccountCreateResponse(
    String accountId,
    String accountNumber,
    String accountType,
    String userId,
    double balance,
    String status
) {}
