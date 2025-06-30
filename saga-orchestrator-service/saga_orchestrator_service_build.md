
# Saga Orchestrator Service Build Log

This document tracks the build process and conversation for the `saga-orchestrator-service`.

## Conversation Summary (Copilot)

**TASK DESCRIPTION:**
Implement a central orchestration process for a user onboarding saga using the command/reply pattern. The orchestrator should manage saga workflows, persist saga and step state, and define workflow and compensation logic centrally. The focus is on the user onboarding saga, using a uniform command/reply topic naming convention, and storing step-specific payloads in SagaStepInstance.

**COMPLETED:**
- Analyzed and explained the user onboarding saga flow, clarified that the orchestrator should initiate all steps (including user creation), and updated the saga definition accordingly.
- Refactored `SagaConfig.java` to use a consistent command/reply naming convention for all steps in the user onboarding saga.
- Modified `SagaInstance` entity to remove the `payload` field and keep only lightweight tracking fields (`sagaName`, `currentStep`, `status`).
- Confirmed and explained the design choice to store step-specific payloads in `SagaStepInstance` only.
- Ensured `SagaStepInstance` entity contains the payload for each step.
- Implemented and/or confirmed existence of repositories: `SagaInstanceRepository` and `SagaStepInstanceRepository`.
- Implemented `SagaOrchestratorImpl`:
  - Injects all required dependencies.
  - `startSaga` creates a `SagaInstance` and an initial `SagaStepInstance` for the input payload, then triggers the first step.
  - `handleReply` finds the saga definition by reply destination, loads the saga instance, and delegates to `handleSuccess` or `handleFailure`.
  - `handleSuccess` creates a completed `SagaStepInstance`, advances the saga, and triggers the next step or marks the saga as completed.
  - `handleFailure` creates a failed `SagaStepInstance`, marks the saga as failed, and triggers compensation logic.
- Restored and implemented `SagaReplyListener` with consumer beans for all user onboarding saga reply topics, delegating to the orchestrator.
- Added `IN_PROGRESS` to `SagaStatus` and removed the redundant `STARTED` status.
- Added `findSagaDefinitionByReplyDestination` to `SagaDefinitionRegistry` to map reply topics to saga definitions.
- Explained the rationale for all major design decisions and code structure.
- Refactored `SagaReplyListener` to dynamically retrieve the destination from message headers instead of using hardcoded values, making it more generic.

**PENDING:**
- (None for the core user onboarding saga orchestration; further enhancements such as full compensation logic, multi-saga support, or advanced error handling may be considered in the future.)

**CODE STATE:**
- /src/main/java/com/rajeswaran/saga/config/SagaConfig.java
- /src/main/java/com/rajeswaran/saga/entity/SagaInstance.java
- /src/main/java/com/rajeswaran/saga/entity/SagaStepInstance.java
- /src/main/java/com/rajeswaran/saga/model/SagaStatus.java
- /src/main/java/com/rajeswaran/saga/model/SagaStepStatus.java
- /src/main/java/com/rajeswaran/saga/repository/SagaInstanceRepository.java
- /src/main/java/com/rajeswaran/saga/repository/SagaStepInstanceRepository.java
- /src/main/java/com/rajeswaran/saga/definition/SagaDefinitionRegistry.java
- /src/main/java/com/rajeswaran/saga/service/SagaOrchestrator.java
- /src/main/java/com/rajeswaran/saga/service/SagaOrchestratorImpl.java
- /src/main/java/com/rajeswaran/saga/listener/SagaReplyListener.java

**CHANGES:**
- Refactored saga step definitions in `SagaConfig.java` to use a uniform command/reply pattern.
- Removed `payload` from `SagaInstance` and ensured it is only in `SagaStepInstance`.
- Added `IN_PROGRESS` to `SagaStatus` and removed `STARTED`.
- Added `findSagaDefinitionByReplyDestination` to `SagaDefinitionRegistry`.
- Implemented `SagaStepInstanceRepository`.
- Refactored `SagaOrchestratorImpl` to:
  - Use `SagaStepInstance` for all payload storage.
  - Track and advance saga steps using `currentStep`.
  - Create step instances for both success and failure, and handle compensation.
- Restored and implemented all relevant consumer beans in `SagaReplyListener` for the user onboarding saga.
- Refactored `SagaReplyListener` to dynamically read the destination topic from message headers.
