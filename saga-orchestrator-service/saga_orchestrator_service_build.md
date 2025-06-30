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
- Restored and implemented `SagaReplyListener` with consumer beans for all user onboarding saga reply topics, delegating to the orchestrator.
- Added `IN_PROGRESS` to `SagaStatus` and removed the redundant `STARTED` status.
- Added `findSagaDefinitionByReplyDestination` to `SagaDefinitionRegistry` to map reply topics to saga definitions.
- Explained the rationale for all major design decisions and code structure.
- Refactored `SagaReplyListener` to dynamically retrieve the destination from message headers instead of using hardcoded values, making it more generic.
- **Implemented full compensation logic**:
  - Added `ROLLED_BACK` status to `SagaStatus` and refined `SagaStepStatus`.
  - Enhanced `SagaOrchestratorImpl` to trigger a compensation workflow on failure.
  - The `compensate` method iterates backward, sends compensation commands for completed steps, and updates step statuses to `COMPENSATED`.
  - Added `findFirstBySagaInstanceAndStepNameAndStatusOrderByCreatedAtDesc` to `SagaStepInstanceRepository` to support the compensation logic.
  - The saga status is set to `ROLLED_BACK` after a successful rollback.
- [2025-06-30] Removed obsolete `UserRegisteredEventListener.java` from `account-service` to complete the transition from saga choreography to orchestration for user onboarding.
- Verified all saga participants, bindings, DTOs, and event contracts are consistent and up-to-date.
- Build process now expects only orchestrated flow for user onboarding.

**PENDING:**
- (None. The core user onboarding saga with happy path and compensation logic is complete.)

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
- Added `IN_PROGRESS` and `ROLLED_BACK` to `SagaStatus`.
- Added `findSagaDefinitionByReplyDestination` to `SagaDefinitionRegistry`.
- Implemented `SagaStepInstanceRepository` with a custom finder method.
- Refactored `SagaOrchestratorImpl` to:
  - Use `SagaStepInstance` for all payload storage.
  - Track and advance saga steps using `currentStep`.
  - Implement a full compensation and rollback mechanism in `handleFailure` and `compensate` methods.
- Restored and implemented all relevant consumer beans in `SagaReplyListener` for the user onboarding saga.
- Refactored `SagaReplyListener` to dynamically read the destination topic from message headers.
- Removed obsolete `UserRegisteredEventListener.java` from `account-service` to complete the transition from saga choreography to orchestration for user onboarding.
- Verified all saga participants, bindings, DTOs, and event contracts are consistent and up-to-date.
- Build process now expects only orchestrated flow for user onboarding.

### [2025-06-30] Build and verification
- User requested full Maven build and verification after removing old saga choreography code from user-service.
- Confirmed build success for all modules and no errors.
- Migration to central orchestrator flow is now clean.

### [2025-06-30] Security configuration for saga-orchestrator-service
- Added SecurityConfig.java to saga-orchestrator-service for OAuth2/JWT and method security, following the pattern from other services.
- Ensures consistent security posture across all services.
- User approved the plan and implementation.

### [2025-06-30] Security property for JWT decoder
- Added spring.security.oauth2.resourceserver.jwt.jwk-set-uri to saga-orchestrator-service/application.yml to resolve missing JwtDecoder bean and align with other services.
- Used the same Keycloak JWK set URI pattern as other services.
