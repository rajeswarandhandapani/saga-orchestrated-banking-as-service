package com.rajeswaran.saga.definition;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaDefinitionRegistry {

    private final Map<String, SagaDefinition> registry = new ConcurrentHashMap<>();

    public void registerSaga(SagaDefinition sagaDefinition) {
        registry.put(sagaDefinition.getSagaName(), sagaDefinition);
    }

    public SagaDefinition getSagaDefinition(String sagaName) {
        return registry.get(sagaName);
    }
}
