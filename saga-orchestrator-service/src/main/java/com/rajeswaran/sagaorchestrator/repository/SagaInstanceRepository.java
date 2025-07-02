package com.rajeswaran.sagaorchestrator.repository;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
}
