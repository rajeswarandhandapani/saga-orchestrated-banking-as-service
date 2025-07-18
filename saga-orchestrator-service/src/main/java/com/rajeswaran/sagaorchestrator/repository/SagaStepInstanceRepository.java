package com.rajeswaran.sagaorchestrator.repository;

import com.rajeswaran.sagaorchestrator.constants.SagaConstants;
import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import com.rajeswaran.sagaorchestrator.entity.SagaStepInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaStepInstanceRepository extends JpaRepository<SagaStepInstance, Long> {
    Optional<SagaStepInstance> findFirstBySagaInstanceAndStepNameAndStatusOrderByCreatedAtDesc(
            SagaInstance sagaInstance, String stepName, SagaConstants.SagaStepStatus status);
    
    Optional<SagaStepInstance> findFirstBySagaInstanceAndStepNameOrderByCreatedAtDesc(
            SagaInstance sagaInstance, String stepName);
}
