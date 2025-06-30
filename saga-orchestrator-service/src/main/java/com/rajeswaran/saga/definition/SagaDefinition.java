package com.rajeswaran.saga.definition;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SagaDefinition {
    private String sagaName;
    private List<SagaStepDefinition> steps;
}
