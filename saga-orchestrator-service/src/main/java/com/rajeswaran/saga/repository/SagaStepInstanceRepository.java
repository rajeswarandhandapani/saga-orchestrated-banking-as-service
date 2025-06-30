package com.rajeswaran.saga.repository;

import com.rajeswaran.saga.entity.SagaInstance;
import com.rajeswaran.saga.entity.SagaStepInstance;
import com.rajeswaran.saga.model.SagaStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaStepInstanceRepository extends JpaRepository<SagaStepInstance, Long> {
    Optional<SagaStepInstance> findFirstBySagaInstanceAndStepNameAndStatusOrderByCreatedAtDesc(
            SagaInstance sagaInstance, String stepName, SagaStepStatus status);
}
