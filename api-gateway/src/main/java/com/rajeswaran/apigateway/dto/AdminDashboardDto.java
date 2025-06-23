package com.rajeswaran.apigateway.dto;

import java.util.List;

public record AdminDashboardDto(
    List<Object> accounts,
    List<Object> payments,
    List<Object> transactions,
    List<Object> auditLogs,
    List<Object> notifications,
    List<Object> users
) {}
