package com.rajeswaran.sagaorchestrator.dto;

public record AccountCreateRequest(
    String accountId,
    String accountNumber,
    String accountType,
    String userId,
    double balance,
    String status
) {}
