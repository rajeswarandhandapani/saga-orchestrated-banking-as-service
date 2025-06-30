package com.rajeswaran.saga.definition;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SagaStepDefinition {
    private String commandDestination;
    private String replyDestination;
    private String compensationDestination;
}
