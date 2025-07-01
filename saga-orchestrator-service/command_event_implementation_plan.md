# Command/Event Implementation Plan for Lightweight Saga Orchestrator

## Project Overview
This document outlines the plan to enhance the existing saga-orchestrator-service with a lightweight command/event pattern for better scalability, maintainability, and observability.

## Current State Analysis
The existing saga-orchestrator-service already has:
- ✅ Basic saga persistence (`SagaInstance`, `SagaStepInstance`)
- ✅ Command/reply pattern via Kafka
- ✅ User onboarding saga with compensation logic
- ✅ Registry-based saga definition system

## Enhancement Plan for Lightweight Command/Event Pattern

### **Phase 1: Core Command/Event Infrastructure**

#### 1.1 Command Framework
- Create base command interfaces and abstractions
- Implement command dispatcher with event sourcing
- Add command validation and serialization
- Command timeout handling mechanism

**Components to implement:**
- `Command` interface
- `CommandHandler<T>` interface
- `CommandDispatcher` service
- `CommandValidation` framework
- `CommandTimeoutManager`

#### 1.2 Event Framework
- Create event base classes and interfaces
- Implement event store for persistence
- Add event replay capability for saga recovery
- Event versioning support

**Components to implement:**
- `DomainEvent` interface
- `EventHandler<T>` interface
- `EventStore` service
- `EventReplayService`
- `EventVersionManager`

#### 1.3 Enhanced Saga Engine
- Refactor saga orchestrator to focus on state management only
- Create domain-specific saga classes (e.g., `UserOnboardingSaga`)
- Implement saga registration and lifecycle management
- State recovery mechanisms

**Components to implement:**
- `Saga` base interface for domain-specific sagas
- `SagaRegistry` for saga class registration
- `SagaStateManager` for state persistence
- `SagaLifecycleManager` for saga coordination

### **Phase 2: User Onboarding Saga Modernization**

#### 2.1 UserOnboardingSaga Class Design
```java
@Component
@Slf4j
public class UserOnboardingSaga {
    
    private final SagaOrchestrator sagaOrchestrator;
    private final StreamBridge streamBridge;
    
    // === COMMAND PRODUCERS (Triggers commands to other services) ===
    
    public void startUserOnboarding(Long sagaId, UserDTO userDto) {
        log.info("Starting user onboarding saga {}", sagaId);
        triggerCreateUserCommand(sagaId, userDto);
    }
    
    private void triggerCreateUserCommand(Long sagaId, UserDTO userDto) {
        CreateUserCommand command = CreateUserCommand.builder()
            .sagaId(sagaId)
            .user(userDto)
            .build();
        streamBridge.send("createUserCommand-out-0", command);
        sagaOrchestrator.recordStep(sagaId, "CreateUser", SagaStepStatus.STARTED);
    }
    
    private void triggerOpenAccountCommand(Long sagaId, UserDTO userDto) {
        OpenAccountCommand command = OpenAccountCommand.builder()
            .sagaId(sagaId)
            .userId(userDto.getId())
            .accountType("SAVINGS")
            .build();
        streamBridge.send("openAccountCommand-out-0", command);
        sagaOrchestrator.recordStep(sagaId, "OpenAccount", SagaStepStatus.STARTED);
    }
    
    private void triggerSendWelcomeNotificationCommand(Long sagaId, UserDTO userDto) {
        SendNotificationCommand command = SendNotificationCommand.builder()
            .sagaId(sagaId)
            .userId(userDto.getId())
            .type("WELCOME")
            .message("Welcome to our banking service!")
            .build();
        streamBridge.send("sendNotificationCommand-out-0", command);
        sagaOrchestrator.recordStep(sagaId, "SendNotification", SagaStepStatus.STARTED);
    }
    
    // === EVENT LISTENERS (Consumes events from other services) ===
    
    @Bean
    public Consumer<Message<UserCreatedEvent>> userCreatedEvent() {
        return message -> {
            UserCreatedEvent event = message.getPayload();
            log.info("User created successfully for saga {}", event.getSagaId());
            
            sagaOrchestrator.recordStep(event.getSagaId(), "CreateUser", SagaStepStatus.COMPLETED);
            
            // Proceed to next step: Open Account
            triggerOpenAccountCommand(event.getSagaId(), event.getUser());
        };
    }
    
    @Bean
    public Consumer<Message<UserCreationFailedEvent>> userCreationFailedEvent() {
        return message -> {
            UserCreationFailedEvent event = message.getPayload();
            log.error("User creation failed for saga {}: {}", event.getSagaId(), event.getError());
            
            sagaOrchestrator.recordStep(event.getSagaId(), "CreateUser", SagaStepStatus.FAILED);
            sagaOrchestrator.compensate(event.getSagaId());
        };
    }
    
    @Bean
    public Consumer<Message<AccountOpenedEvent>> accountOpenedEvent() {
        return message -> {
            AccountOpenedEvent event = message.getPayload();
            log.info("Account opened successfully for saga {}", event.getSagaId());
            
            sagaOrchestrator.recordStep(event.getSagaId(), "OpenAccount", SagaStepStatus.COMPLETED);
            
            // Proceed to next step: Send Welcome Notification
            UserDTO userDto = getUserFromSaga(event.getSagaId()); // Helper method
            triggerSendWelcomeNotificationCommand(event.getSagaId(), userDto);
        };
    }
    
    @Bean
    public Consumer<Message<AccountOpenFailedEvent>> accountOpenFailedEvent() {
        return message -> {
            AccountOpenFailedEvent event = message.getPayload();
            log.error("Account opening failed for saga {}: {}", event.getSagaId(), event.getError());
            
            sagaOrchestrator.recordStep(event.getSagaId(), "OpenAccount", SagaStepStatus.FAILED);
            sagaOrchestrator.compensate(event.getSagaId());
        };
    }
    
    @Bean
    public Consumer<Message<NotificationSentEvent>> notificationSentEvent() {
        return message -> {
            NotificationSentEvent event = message.getPayload();
            log.info("Welcome notification sent for saga {}", event.getSagaId());
            
            sagaOrchestrator.recordStep(event.getSagaId(), "SendNotification", SagaStepStatus.COMPLETED);
            
            // Saga completed successfully
            sagaOrchestrator.updateSagaState(event.getSagaId(), SagaStatus.COMPLETED);
            log.info("User onboarding saga {} completed successfully", event.getSagaId());
        };
    }
    
    @Bean
    public Consumer<Message<NotificationFailedEvent>> notificationFailedEvent() {
        return message -> {
            NotificationFailedEvent event = message.getPayload();
            log.warn("Welcome notification failed for saga {}: {}", event.getSagaId(), event.getError());
            
            // Note: Notification failure might not require compensation
            // depending on business requirements
            sagaOrchestrator.recordStep(event.getSagaId(), "SendNotification", SagaStepStatus.FAILED);
            sagaOrchestrator.updateSagaState(event.getSagaId(), SagaStatus.COMPLETED_WITH_WARNINGS);
        };
    }
    
    // === COMPENSATION HANDLERS ===
    
    public void compensateUserOnboarding(Long sagaId) {
        log.info("Starting compensation for user onboarding saga {}", sagaId);
        // Compensation logic - delete created resources in reverse order
        // Implementation depends on which steps were completed
    }
    
    // === HELPER METHODS ===
    
    private UserDTO getUserFromSaga(Long sagaId) {
        // Retrieve user data from saga state or step instances
        return sagaOrchestrator.getSagaData(sagaId, UserDTO.class);
    }
}
```

#### 2.2 SagaOrchestrator as State Manager
```java
@Service
public class SagaOrchestrator {
    // State management only - no business logic
    public SagaInstance startSaga(String sagaName, Map<String, Object> payload);
    public void updateSagaState(Long sagaId, SagaStatus status);
    public void recordStep(Long sagaId, String stepName, SagaStepStatus status);
    public void compensate(Long sagaId);
    public <T> T getSagaData(Long sagaId, Class<T> dataType);
    
    // Delegates saga execution to appropriate saga classes
    public void executeSaga(String sagaName, Long sagaId, Map<String, Object> payload) {
        switch(sagaName) {
            case "user-onboarding-saga":
                userOnboardingSaga.startUserOnboarding(sagaId, (UserDTO) payload.get("user"));
                break;
            // Future sagas will be added here
        }
    }
}
```

#### 2.3 Message Flow Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                    UserOnboardingSaga.java                     │
├─────────────────────────────────────────────────────────────────┤
│ Command Producers:           │ Event Listeners:                │
│ • triggerCreateUserCommand   │ • userCreatedEvent              │
│ • triggerOpenAccountCommand  │ • userCreationFailedEvent       │
│ • triggerSendNotification... │ • accountOpenedEvent            │
│                              │ • accountOpenFailedEvent        │
│                              │ • notificationSentEvent         │
│                              │ • notificationFailedEvent       │
└─────────────────────────────────────────────────────────────────┘
              │                              ▲
              ▼ (Commands)                   │ (Events)
┌─────────────────────────────────────────────────────────────────┐
│                      Kafka Topics                              │
│ • createUserCommand-out-0    │ • userCreatedEvent-in-0         │
│ • openAccountCommand-out-0   │ • userCreationFailedEvent-in-0  │
│ • sendNotificationCommand... │ • accountOpenedEvent-in-0       │
└─────────────────────────────────────────────────────────────────┘
              │                              ▲
              ▼                              │
┌─────────────────────────────────────────────────────────────────┐
│              External Services                                  │
│ • user-service               │ • account-service               │
│ • notification-service       │ • audit-service                 │
└─────────────────────────────────────────────────────────────────┘
```

### **Phase 3: Advanced Features**

#### 3.1 Saga State Management
- Event-sourced saga state
- Snapshot support for performance
- State recovery mechanisms
- Saga state versioning

#### 3.2 Monitoring & Observability
- Saga execution metrics
- Event tracing
- Command audit logs
- Performance monitoring
- Dead letter queue handling

#### 3.3 Resilience Features
- Circuit breaker pattern for external calls
- Retry mechanisms with exponential backoff
- Bulkhead pattern for resource isolation
- Health checks for saga participants

## **Technical Architecture (Updated)**

```
┌─────────────────┐    ┌─────────────────┐    ┌──────────────────────────────┐
│   API Gateway   │────│ Saga Controller │────│    UserOnboardingSaga        │
└─────────────────┘    └─────────────────┘    │ ┌─────────────────────────┐  │
                                │              │ │ Command Producers:      │  │
                                ▼              │ │ • triggerCreateUser     │  │
                       ┌─────────────────┐     │ │ • triggerOpenAccount    │  │
                       │ SagaOrchestrator│◄────│ │ • triggerNotification   │  │
                       │ (State Manager) │     │ └─────────────────────────┘  │
                       └─────────────────┘     │ ┌─────────────────────────┐  │
                                │              │ │ Event Listeners:        │  │
                                ▼              │ │ • userCreatedEvent      │  │
                       ┌─────────────────┐     │ │ • accountOpenedEvent    │  │
                       │  Event Store    │     │ │ • notificationSent...   │  │
                       └─────────────────┘     │ └─────────────────────────┘  │
                                │              └──────────────────────────────┘
                                ▼                               │        ▲
                       ┌─────────────────┐                     │        │
                       │   Kafka Topics  │◄────────────────────┘        │
                       │                 │──────────────────────────────┘
                       └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ External Srvcs  │
                       │ (user, account, │
                       │  notification)  │
                       └─────────────────┘
```

### **New Architecture Principles:**
1. **Command Producer Pattern**: `UserOnboardingSaga` produces commands to external services via Kafka
2. **Event Consumer Pattern**: Same saga class consumes response events from those services  
3. **Self-Orchestrating Sagas**: Each saga manages its own flow progression based on events
4. **State Management Delegation**: SagaOrchestrator handles only persistence and state tracking
5. **Bi-directional Communication**: Commands out → Events in → Next command out → Next event in...

### **Complete Flow Example:**
```
UserOnboardingSaga.startUserOnboarding()
    ↓ produces
CreateUserCommand → user-service
    ↓ responds with  
UserCreatedEvent → UserOnboardingSaga.userCreatedEvent()
    ↓ triggers
OpenAccountCommand → account-service  
    ↓ responds with
AccountOpenedEvent → UserOnboardingSaga.accountOpenedEvent()
    ↓ triggers
SendNotificationCommand → notification-service
    ↓ responds with
NotificationSentEvent → UserOnboardingSaga.notificationSentEvent()
    ↓ completes saga
SagaOrchestrator.updateSagaState(COMPLETED)
```

## **Dependencies to Add**

### Core Dependencies
```xml
<!-- Event Sourcing and CQRS -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Enhanced JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>

<!-- Scheduling for Timeouts -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>

<!-- Metrics and Monitoring -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Optional Dependencies (for advanced features)
```xml
<!-- Redis for distributed locking and caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Resilience4j for circuit breaker -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

## **Implementation Steps**

### **Step 0: Code Analysis and Preservation Strategy**

#### **Step 0.1: Assess Current Command/Reply Implementation**
1. ✅ **KEEP**: Existing `SagaStepDefinition` with command/reply destinations
2. ✅ **KEEP**: `SagaReplyListener` infrastructure  
3. ✅ **KEEP**: `SagaInstance` and `SagaStepInstance` entities
4. ✅ **KEEP**: `SagaDefinitionRegistry` and `SagaConfig`
5. ✅ **KEEP**: Existing Kafka integration via Spring Cloud Stream

#### **Step 0.2: Identify Enhancement Points**
1. **Extend** `SagaOrchestrator` interface with command/event support
2. **Add** event sourcing layer on top of existing persistence
3. **Enhance** existing command dispatch with validation and metrics
4. **Preserve** backward compatibility with current user-onboarding-saga

#### **Step 0.3: Backward Compatibility Strategy**
1. Maintain existing API contracts
2. Keep current Kafka topic naming conventions
3. Preserve existing saga definitions during transition
4. Ensure current user-onboarding-saga continues to work

### **Step 1: Command/Event Base Infrastructure**

#### **Step 1.1: Project Setup and Dependencies**
1. Update parent `pom.xml` with new dependency versions
2. Add core dependencies to saga-orchestrator-service `pom.xml`
3. Verify Java 21 compatibility for all dependencies
4. Create package structure for command/event framework

#### **Step 1.2: Base Command Framework**
1. Create `Command` marker interface
2. Create `CommandMetadata` class for command tracking
3. Create `BaseCommand` abstract class with common properties
4. Create `CommandResult` class for command execution results
5. Create `CommandHandler<T extends Command>` interface

#### **Step 1.3: Command Validation Framework**
1. Create `CommandValidator<T extends Command>` interface
2. Create `ValidationResult` class for validation outcomes
3. Create `CompositeCommandValidator` for multiple validations
4. Implement basic validation annotations and processors

#### **Step 1.4: Command Dispatcher Infrastructure**
1. Create `CommandDispatcher` interface
2. Create `CommandDispatcherImpl` with Spring integration
3. Create `CommandHandlerRegistry` for handler registration
4. Add command execution logging and metrics hooks
5. Implement command routing based on command type

#### **Step 1.5: Base Event Framework**
1. Create `DomainEvent` marker interface
2. Create `EventMetadata` class for event tracking
3. Create `BaseEvent` abstract class with common properties
4. Create `EventHandler<T extends DomainEvent>` interface
5. Create `EventResult` class for event processing results

#### **Step 1.6: Event Store Foundation**
1. Create `EventStore` interface with basic CRUD operations
2. Create `EventStoreEntity` JPA entity for event persistence
3. Create `EventStoreRepository` with custom queries
4. Create `EventSerializer` for JSON serialization/deserialization
5. Create `EventStoreImpl` with database persistence

#### **Step 1.7: Event Dispatcher and Handler Registry**
1. Create `EventDispatcher` interface
2. Create `EventDispatcherImpl` with async processing
3. Create `EventHandlerRegistry` for handler registration
4. Add event processing logging and metrics hooks
5. Implement event routing based on event type

#### **Step 1.8: Integration and Configuration**
1. Create `CommandEventConfig` Spring configuration class
2. Create auto-configuration for command/event components
3. Add `@EnableCommandEventFramework` annotation
4. Create application properties for framework configuration
5. Add framework initialization and health checks

### **Step 2: Event Store Implementation**
1. Design event schema and persistence
2. Implement event serialization/deserialization
3. Add event replay capabilities
4. Create event versioning system

### **Step 3: Enhanced Saga Orchestrator**
1. Extend existing orchestrator with command/event support
2. Implement timeout handling
3. Add state recovery mechanisms
4. Create saga state snapshots

### **Step 4: UserOnboardingSaga Implementation**
1. Create `UserOnboardingSaga.java` with all command and event handlers
2. Implement saga flow coordination within the saga class
3. Integrate with SagaOrchestrator for state management
4. Add compensation logic within the saga
5. Create command and event classes for user onboarding

### **Step 5: SagaOrchestrator Refactoring**
1. Refactor SagaOrchestrator to focus on state management only
2. Remove business logic and move to saga classes
3. Implement saga registration and discovery
4. Add saga lifecycle management
5. Maintain backward compatibility with existing sagas

### **Step 5: Monitoring and Metrics**
1. Add saga execution metrics
2. Implement event tracing
3. Create command audit logs
4. Set up performance monitoring

### **Step 6: Testing and Validation**
1. Unit tests for all components
2. Integration tests for saga flows
3. Performance testing
4. Resilience testing

## **Directory Structure (Updated)**

```
saga-orchestrator-service/
├── src/main/java/com/rajeswaran/
│   ├── command/
│   │   ├── base/
│   │   │   ├── Command.java
│   │   │   ├── CommandHandler.java
│   │   │   └── CommandDispatcher.java
│   │   └── validation/
│   ├── event/
│   │   ├── base/
│   │   │   ├── DomainEvent.java
│   │   │   ├── EventHandler.java
│   │   │   └── EventStore.java
│   │   └── store/
│   ├── saga/
│   │   ├── base/
│   │   │   ├── Saga.java (base interface)
│   │   │   ├── SagaStateManager.java
│   │   │   └── SagaRegistry.java
│   │   ├── orchestrator/
│   │   │   └── SagaOrchestrator.java (state manager only)
│   │   └── useronboarding/
│   │       ├── UserOnboardingSaga.java (all handlers)
│   │       ├── commands/
│   │       │   ├── CreateUserCommand.java
│   │       │   ├── CreateAccountCommand.java
│   │       │   └── SendWelcomeNotificationCommand.java
│   │       └── events/
│   │           ├── UserCreatedEvent.java
│   │           ├── AccountCreatedEvent.java
│   │           └── WelcomeNotificationSentEvent.java
│   └── config/
│       └── CommandEventConfig.java
├── command_event_implementation_plan.md
└── saga_orchestrator_service_build.md
```

## **Success Criteria**

1. **Functional Requirements**
   - User onboarding saga works with command/event pattern
   - Compensation logic functions correctly
   - Timeout handling works as expected
   - State recovery works after failures

2. **Non-Functional Requirements**
   - Performance: < 100ms for command processing
   - Reliability: 99.9% saga completion rate
   - Scalability: Handle 1000+ concurrent sagas
   - Observability: Full tracing and metrics

3. **Code Quality**
   - 90%+ test coverage
   - Clean architecture principles
   - SOLID design principles
   - Comprehensive documentation

## **Risk Mitigation**

1. **Data Consistency**: Use event sourcing for reliable state management
2. **Performance**: Implement caching and snapshots for large sagas
3. **Complexity**: Start with simple implementation and iterate
4. **Integration**: Maintain backward compatibility during migration

## **Timeline Estimation**

- **Phase 1**: 2-3 days (Base infrastructure)
- **Phase 2**: 3-4 days (User onboarding saga)
- **Phase 3**: 2-3 days (Advanced features)
- **Testing & Documentation**: 1-2 days

**Total Estimated Time**: 8-12 days

---

**Last Updated**: July 1, 2025
**Version**: 1.0
**Status**: Planning Phase
