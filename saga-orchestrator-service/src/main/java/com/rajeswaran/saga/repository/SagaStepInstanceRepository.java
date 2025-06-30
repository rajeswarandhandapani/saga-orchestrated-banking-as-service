package com.rajeswaran.saga.repository;

import com.rajeswaran.saga.entity.SagaStepInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaStepInstanceRepository extends JpaRepository<SagaStepInstance, Long> {
}
