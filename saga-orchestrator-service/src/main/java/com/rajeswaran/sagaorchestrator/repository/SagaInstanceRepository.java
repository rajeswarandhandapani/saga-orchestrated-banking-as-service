package com.rajeswaran.sagaorchestrator.repository;

import com.rajeswaran.sagaorchestrator.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {
    @Query("SELECT DISTINCT s FROM SagaInstance s LEFT JOIN FETCH s.stepInstances ORDER BY s.createdAt DESC")
    List<SagaInstance> findAllWithStepInstances();
}
