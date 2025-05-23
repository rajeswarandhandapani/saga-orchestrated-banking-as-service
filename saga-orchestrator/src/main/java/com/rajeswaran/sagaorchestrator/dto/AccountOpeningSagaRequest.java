package com.rajeswaran.sagaorchestrator.dto;

public record AccountOpeningSagaRequest(
    String username,
    String email,
    String fullName,
    String accountType
) {}
