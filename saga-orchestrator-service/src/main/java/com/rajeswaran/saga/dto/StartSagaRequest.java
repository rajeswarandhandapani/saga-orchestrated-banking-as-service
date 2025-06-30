package com.rajeswaran.saga.dto;

import lombok.Data;

@Data
public class StartSagaRequest {
    private String sagaName;
    private String payload;
}
