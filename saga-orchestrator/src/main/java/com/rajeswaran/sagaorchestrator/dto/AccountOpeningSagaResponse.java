package com.rajeswaran.sagaorchestrator.dto;

public record AccountOpeningSagaResponse(
    String status,
    String message,
    String userId,
    String accountId
) {}
